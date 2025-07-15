provider "aws" {
  region = var.aws_region
}

# Random string for unique resource names
resource "random_string" "suffix" {
  length  = 8
  special = false
  upper   = false
}

# VPC Module (Free Tier: public subnets only, no NAT Gateway, disable flow logs)
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.8"

  name = "${var.app_name}-vpc"
  cidr = "10.0.0.0/16"

  azs            = ["${var.aws_region}a", "${var.aws_region}b"]
  public_subnets = ["10.0.1.0/24", "10.0.2.0/24"]

  enable_nat_gateway                   = false
  enable_dns_hostnames                 = true
  map_public_ip_on_launch              = true
  enable_flow_log                      = false
  create_flow_log_cloudwatch_log_group = false
  create_flow_log_cloudwatch_iam_role  = false

  tags = {
    Name = "${var.app_name}-vpc"
  }
}

# ECR Repository
resource "aws_ecr_repository" "app" {
  name                 = "${var.app_name}-repo"
  image_tag_mutability = "MUTABLE"

  tags = {
    Name = "${var.app_name}-repo"
  }
}

# RDS MySQL (Free Tier: db.t3.micro, 20GB storage)
module "rds" {
  source  = "terraform-aws-modules/rds/aws"
  version = "~> 6.0"

  identifier = "${var.app_name}-db"

  engine            = "mysql"
  engine_version    = "8.0"
  instance_class    = "db.t3.micro"
  allocated_storage = 20
  storage_encrypted = true

  username = var.mysql_user
  password = var.mysql_password
  port     = var.mysql_port
  db_name  = var.mysql_database

  vpc_security_group_ids = [aws_security_group.rds_sg.id]
  db_subnet_group_name   = aws_db_subnet_group.rds.name
  multi_az               = false
  publicly_accessible    = false
  skip_final_snapshot    = true

  family               = "mysql8.0"
  major_engine_version = "8.0"

  tags = {
    Name = "${var.app_name}-db"
  }
}

resource "aws_db_subnet_group" "rds" {
  name       = "${var.app_name}-db-subnet-group"
  subnet_ids = module.vpc.public_subnets

  tags = {
    Name = "${var.app_name}-db-subnet-group"
  }
}

# ElastiCache Redis (Free Tier: cache.t3.micro)
resource "aws_elasticache_cluster" "redis" {
  cluster_id           = "${var.app_name}-redis"
  engine               = "redis"
  node_type            = "cache.t3.micro"
  num_cache_nodes      = 1
  parameter_group_name = "default.redis7"
  engine_version       = "7.0"
  port                 = var.redis_port
  security_group_ids   = [aws_security_group.redis_sg.id]
  subnet_group_name    = aws_elasticache_subnet_group.redis.name

  tags = {
    Name = "${var.app_name}-redis"
  }
}

resource "aws_elasticache_subnet_group" "redis" {
  name       = "${var.app_name}-redis-subnet-group"
  subnet_ids = module.vpc.public_subnets

  tags = {
    Name = "${var.app_name}-redis-subnet-group"
  }
}

# Elastic Beanstalk Application
resource "aws_elastic_beanstalk_application" "app" {
  name        = var.app_name
  description = "Spring Boot Application"
}

# Elastic Beanstalk Application Version
resource "aws_elastic_beanstalk_application_version" "app_version" {
  name        = "${var.app_name}-version-${random_string.suffix.result}"
  application = aws_elastic_beanstalk_application.app.name
  bucket      = aws_s3_bucket.app_bundle.id
  key         = aws_s3_object.dockerrun.key

  depends_on = [aws_s3_object.dockerrun]
}

# S3 Bucket for Application Bundle
resource "aws_s3_bucket" "app_bundle" {
  bucket = "${var.app_name}-app-bundle-${random_string.suffix.result}"

  tags = {
    Name = "${var.app_name}-app-bundle"
  }
}

# S3 Bucket Server-Side Encryption
resource "aws_s3_bucket_server_side_encryption_configuration" "app_bundle_encryption" {
  bucket = aws_s3_bucket.app_bundle.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# Upload Dockerrun.aws.json to S3
resource "aws_s3_object" "dockerrun" {
  bucket = aws_s3_bucket.app_bundle.id
  key    = "Dockerrun.aws.json"
  source = local_file.dockerrun.filename

  depends_on = [local_file.dockerrun]
}

# Local file for Dockerrun.aws.json
resource "local_file" "dockerrun" {
  content = jsonencode({
    AWSEBDockerrunVersion = "1"
    Image = {
      Name = "${aws_ecr_repository.app.repository_url}:${var.docker_image_tag}"
    }
    Ports = [
      {
        ContainerPort = 8000
        HostPort      = 8000
      }
    ]
    Volumes = []
    Logging = "/var/log/nginx"
  })
  filename = "${path.module}/Dockerrun.aws.json"
}

# Elastic Beanstalk Environment
resource "aws_elastic_beanstalk_environment" "env" {
  name                = "${var.app_name}-env"
  application         = aws_elastic_beanstalk_application.app.name
  solution_stack_name = "64bit Amazon Linux 2023 v4.5.2 running Docker"
  version_label       = aws_elastic_beanstalk_application_version.app_version.name
  cname_prefix        = "${var.app_name}-env"

  setting {
    namespace = "aws:autoscaling:launchconfiguration"
    name      = "InstanceType"
    value     = "t3.micro"
  }

  setting {
    namespace = "aws:elasticbeanstalk:environment:process:default"
    name      = "Port"
    value     = "80"
  }

  setting {
    namespace = "aws:elasticbeanstalk:environment:process:default"
    name      = "ContainerPort"
    value     = "8000"
  }

  setting {
    namespace = "aws:ec2:vpc"
    name      = "VPCId"
    value     = module.vpc.vpc_id
  }

  setting {
    namespace = "aws:ec2:vpc"
    name      = "Subnets"
    value     = join(",", module.vpc.public_subnets)
  }

  setting {
    namespace = "aws:ec2:vpc"
    name      = "ELBSubnets"
    value     = join(",", module.vpc.public_subnets)
  }

  setting {
    namespace = "aws:autoscaling:launchconfiguration"
    name      = "SecurityGroups"
    value     = aws_security_group.eb_sg.id
  }

  setting {
    namespace = "aws:autoscaling:launchconfiguration"
    name      = "IamInstanceProfile"
    value     = aws_iam_instance_profile.eb_profile.name
  }

  setting {
    namespace = "aws:autoscaling:launchconfiguration"
    name      = "EC2KeyName"
    value     = aws_key_pair.eb_ssh_key.key_name
  }

  dynamic "setting" {
    for_each = local.eb_environment_variables
    content {
      namespace = "aws:elasticbeanstalk:application:environment"
      name      = setting.key
      value     = setting.value
    }
  }

  setting {
    namespace = "aws:elasticbeanstalk:command"
    name      = "DeploymentPolicy"
    value     = "Rolling"
  }

  depends_on = [module.rds, aws_elasticache_cluster.redis]
}

# SSH Key Pair for EC2 access
resource "aws_key_pair" "eb_ssh_key" {
  key_name   = "usf277"
  public_key = file("~/.ssh/id_rsa.pub") # Replace with your public key path
}

# IAM Role for Elastic Beanstalk
resource "aws_iam_role" "eb_role" {
  name = "${var.app_name}-eb-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name = "${var.app_name}-eb-role"
  }
}

# Custom IAM Policy for ECR Access
resource "aws_iam_policy" "ecr_access_policy" {
  name        = "${var.app_name}-ecr-access-policy"
  description = "Policy to allow Elastic Beanstalk to access private ECR repository"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:BatchCheckLayerAvailability"
        ]
        Resource = aws_ecr_repository.app.arn
      }
    ]
  })
}

# Attach Elastic Beanstalk Managed Policies
resource "aws_iam_role_policy_attachment" "eb_web_tier" {
  role       = aws_iam_role.eb_role.name
  policy_arn = "arn:aws:iam::aws:policy/AWSElasticBeanstalkWebTier"
}

resource "aws_iam_role_policy_attachment" "eb_worker_tier" {
  role       = aws_iam_role.eb_role.name
  policy_arn = "arn:aws:iam::aws:policy/AWSElasticBeanstalkWorkerTier"
}

resource "aws_iam_role_policy_attachment" "eb_ecr_custom" {
  role       = aws_iam_role.eb_role.name
  policy_arn = aws_iam_policy.ecr_access_policy.arn
}

# IAM Instance Profile
resource "aws_iam_instance_profile" "eb_profile" {
  name = "${var.app_name}-eb-profile"
  role = aws_iam_role.eb_role.name
}

# Security Groups
resource "aws_security_group" "eb_sg" {
  name        = "${var.app_name}-eb-sg-${random_string.suffix.result}"
  description = "Security group for Elastic Beanstalk"
  vpc_id      = module.vpc.vpc_id

  ingress {
    from_port   = 8000
    to_port     = 8000
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["154.237.203.158/32"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.app_name}-eb-sg"
  }
}

resource "aws_security_group" "rds_sg" {
  name        = "${var.app_name}-rds-sg-${random_string.suffix.result}"
  description = "Security group for RDS"
  vpc_id      = module.vpc.vpc_id

  ingress {
    from_port       = var.mysql_port
    to_port         = var.mysql_port
    protocol        = "tcp"
    security_groups = [aws_security_group.eb_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.app_name}-rds-sg"
  }
}

resource "aws_security_group" "redis_sg" {
  name        = "${var.app_name}-redis-sg-${random_string.suffix.result}"
  description = "Security group for Redis"
  vpc_id      = module.vpc.vpc_id

  ingress {
    from_port       = var.redis_port
    to_port         = var.redis_port
    protocol        = "tcp"
    security_groups = [aws_security_group.eb_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.app_name}-redis-sg"
  }
}

# Local variables for environment settings
locals {
  firebase_config_base64 = base64encode(var.firebase_config)
  eb_environment_variables = {
    MYSQLDATABASE              = var.mysql_database
    MYSQLHOST                  = module.rds.db_instance_address
    MYSQLPORT                  = tostring(var.mysql_port)
    MYSQLUSER                  = var.mysql_user
    MYSQLPASSWORD              = var.mysql_password
    SPRING_DATA_REDIS_HOST     = aws_elasticache_cluster.redis.cache_nodes[0].address
    SPRING_DATA_REDIS_PORT     = tostring(var.redis_port)
    SPRING_DATA_REDIS_PASSWORD = var.redis_password
    SPRING_MAIL_HOST           = var.mail_username != "" ? "smtp.titan.email" : null
    SPRING_MAIL_PORT           = "587"
    SPRING_MAIL_USERNAME       = var.mail_username
    SPRING_MAIL_PASSWORD       = var.mail_password
    CLOUDINARY_CLOUD_NAME      = var.cloudinary_cloud_name
    CLOUDINARY_API_KEY         = var.cloudinary_api_key
    CLOUDINARY_API_SECRET      = var.cloudinary_api_secret
    FIREBASE_CONFIG            = local.firebase_config_base64
    SERVER_PORT                = "8000"
    STRIPE_SECRET              = var.stripe_secret
    # Removed PRODUCTION_DOMAIN to break the cycle
  }
}

# Outputs
output "ecr_repository_url" {
  value       = aws_ecr_repository.app.repository_url
  description = "URL of the ECR repository for the application"
}

output "eb_environment_url" {
  value       = aws_elastic_beanstalk_environment.env.cname
  description = "URL of the Elastic Beanstalk environment"
}

output "rds_endpoint" {
  value       = module.rds.db_instance_address
  description = "Endpoint of the RDS MySQL instance"
}

output "redis_endpoint" {
  value       = aws_elasticache_cluster.redis.cache_nodes[0].address
  description = "Endpoint of the Redis instance"
}
