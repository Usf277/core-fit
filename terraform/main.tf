provider "aws" {
  region = var.aws_region
}

# VPC Module (Free Tier eligible with default settings)
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "5.1.2"

  name = "${var.app_name}-vpc"
  cidr = "10.0.0.0/16"

  azs             = ["${var.aws_region}a", "${var.aws_region}b"]
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24"]
  public_subnets  = ["10.0.101.0/24", "10.0.102.0/24"]

  enable_nat_gateway   = false
  single_nat_gateway   = false
  enable_dns_hostnames = true
  map_public_ip_on_launch = true  # Ensures public IPs for instances in public subnets

  tags = {
    Name = "${var.app_name}-vpc"
  }
}

# ECR Repository for Docker Image 
resource "aws_ecr_repository" "app" {
  name                 = "${var.app_name}-repo"
  image_tag_mutability = "MUTABLE"

  tags = {
    Name = "${var.app_name}-repo"
  }
}

# S3 Bucket for storing Dockerrun.aws.json
resource "aws_s3_bucket" "app_storage" {
  bucket = "${var.app_name}-docker-app-${random_string.suffix.result}"

  tags = {
    Name = "${var.app_name}-docker-app"
  }
}

resource "random_string" "suffix" {
  length  = 8
  special = false
  upper   = false
}

resource "aws_s3_object" "app_bundle" {
  bucket     = aws_s3_bucket.app_storage.id
  key        = "Dockerrun.aws.json"
  source     = "${path.module}/Dockerrun.aws.json"
  depends_on = [local_file.dockerrun]
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

  family               = "mysql8.0"
  major_engine_version = "8.0"
  publicly_accessible  = false
  skip_final_snapshot  = true

  tags = {
    Name = "${var.app_name}-db"
  }
}

# ElastiCache Redis (Free Tier: cache.t3.micro, 0.25GB)
resource "aws_elasticache_cluster" "redis" {
  cluster_id           = "${var.app_name}-redis"
  engine               = "redis"
  node_type            = "cache.t3.micro"
  num_cache_nodes      = 1
  parameter_group_name = "default.redis7"
  engine_version       = "7.0"
  port                 = var.redis_port
  security_group_ids   = [aws_security_group.redis_sg.id]
  subnet_group_name    = aws_elasticache_subnet_group.redis_subnet_group.name

  tags = {
    Name = "${var.app_name}-redis"
  }
}

resource "aws_elasticache_subnet_group" "redis_subnet_group" {
  name       = "${var.app_name}-redis-subnet-group"
  subnet_ids = module.vpc.private_subnets
}

# Elastic Beanstalk Application
resource "aws_elastic_beanstalk_application" "app" {
  name        = var.app_name
  description = "CoreFit Spring Boot Application"
}

# Elastic Beanstalk Application Version
resource "aws_elastic_beanstalk_application_version" "app_version" {
  name        = "${var.app_name}-v1"
  application = aws_elastic_beanstalk_application.app.name
  bucket      = aws_s3_bucket.app_storage.id
  key         = aws_s3_object.app_bundle.key
  description = "CoreFit application version"
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

# Attach Elastic Beanstalk Managed Policies
resource "aws_iam_role_policy_attachment" "eb_web_tier" {
  role       = aws_iam_role.eb_role.name
  policy_arn = "arn:aws:iam::aws:policy/AWSElasticBeanstalkWebTier"
}

resource "aws_iam_role_policy_attachment" "eb_worker_tier" {
  role       = aws_iam_role.eb_role.name
  policy_arn = "arn:aws:iam::aws:policy/AWSElasticBeanstalkWorkerTier"
}

# IAM Instance Profile
resource "aws_iam_instance_profile" "eb_profile" {
  name = "${var.app_name}-eb-profile"
  role = aws_iam_role.eb_role.name
}

# Elastic Beanstalk Environment
resource "aws_elastic_beanstalk_environment" "env" {
  name                = "${var.app_name}-env"
  application         = aws_elastic_beanstalk_application.app.name
  solution_stack_name = "64bit Amazon Linux 2023 v4.5.2 running Docker"
  cname_prefix        = "${var.app_name}-env"
  version_label       = aws_elastic_beanstalk_application_version.app_version.name

  setting {
    namespace = "aws:autoscaling:launchconfiguration"
    name      = "InstanceType"
    value     = "t3.micro"
  }

  setting {
    namespace = "aws:elasticbeanstalk:environment:process:default"
    name      = "Port"
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
    value     = "usf277"  # Updated to match your key pair
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

  depends_on = [aws_s3_object.app_bundle, aws_elastic_beanstalk_application_version.app_version]
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
    cidr_blocks = ["0.0.0.0/0"]  # Open to all for easy access (insecure)
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

resource "aws_db_subnet_group" "rds" {
  name       = "${var.app_name}-rds-subnet-group"
  subnet_ids = module.vpc.private_subnets
  tags = {
    Name = "${var.app_name}-rds-subnet-group"
  }
}

resource "aws_security_group" "rds_sg" {
  name        = "${var.app_name}-rds-sg-${random_string.suffix.result}"
  description = "Security group for RDS"
  vpc_id      = module.vpc.vpc_id

  ingress {
    from_port       = 3306
    to_port         = 3306
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
    from_port       = 6379
    to_port         = 6379
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

# Local file for Dockerrun.aws.json
resource "local_file" "dockerrun" {
  content = jsonencode({
    AWSEBDockerrunVersion = "1"
    Image = {
      Name = "${aws_ecr_repository.app.repository_url}:latest"
    }
    Ports = [
      {
        ContainerPort = 8000
        HostPort      = 8000
      }
    ]
    Volumes = []
    Logging = "/var/log/nginx"
    Environment = {
      MYSQLDATABASE   = var.mysql_database
      MYSQLUSER       = var.mysql_user
      MYSQLPASSWORD   = var.mysql_password
      MYSQLHOST       = module.rds.db_instance_address
      MYSQLPORT       = tostring(var.mysql_port)
      RedisHost       = aws_elasticache_cluster.redis.cache_nodes[0].address
      RedisPort       = tostring(var.redis_port)
      RedisPassword   = var.redis_password
      CloudName       = var.cloudinary_cloud_name
      CloudApiKey     = var.cloudinary_api_key
      CloudApiSecret  = var.cloudinary_api_secret
      FIREBASE_CONFIG = var.firebase_config
      PORT            = "8000"
    }
  })
  filename = "${path.module}/Dockerrun.aws.json"
}

locals {
  eb_environment_variables = {
    MYSQLDATABASE   = var.mysql_database
    MYSQLUSER       = var.mysql_user
    MYSQLPASSWORD   = var.mysql_password
    MYSQLHOST       = module.rds.db_instance_address
    MYSQLPORT       = tostring(var.mysql_port)
    RedisHost       = aws_elasticache_cluster.redis.cache_nodes[0].address
    RedisPort       = tostring(var.redis_port)
    RedisPassword   = var.redis_password
    CloudName       = var.cloudinary_cloud_name
    CloudApiKey     = var.cloudinary_api_key
    CloudApiSecret  = var.cloudinary_api_secret
    FIREBASE_CONFIG = var.firebase_config
    PORT            = "8000"
  }
}

output "ecr_repository_url" {
  value       = aws_ecr_repository.app.repository_url
  description = "URL of the ECR repository for the application"
}

output "eb_environment_url" {
  value       = aws_elastic_beanstalk_environment.env.cname
  description = "URL of the Elastic Beanstalk environment"
}