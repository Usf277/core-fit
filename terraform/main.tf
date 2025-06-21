

## Create Redis Elasticache
resource "aws_elasticache_cluster" "redis_cache" {
  cluster_id           = "cluster-example"
  engine               = "redis"
  node_type            = "cache.m4.large"
  num_cache_nodes      = 1
  parameter_group_name = "default.redis3.2"
  engine_version       = "3.2.10"
  port                 = 6379
}

## Create MySql Database RDS
resource "aws_db_instance" "mysql_database" {
  allocated_storage    = 10
  db_name              = var.mysql_database
  engine               = "mysql"
  engine_version       = "8.0"
  instance_class       = "db.t3.micro"
  username             = "foo"
  password             = "foobarbaz"
  parameter_group_name = "default.mysql8.0"
  skip_final_snapshot  = true
}

## Create ElasticBeanstalk environment


resource "aws_elastic_beanstalk_application" "tftest" {
  name        = "tf-test-name"
  description = "tf-test-desc"
}

resource "aws_elastic_beanstalk_environment" "tfenvtest" {
  name                = "tf-test-name"
  application         = aws_elastic_beanstalk_application.tftest.name
  solution_stack_name = "64bit Amazon Linux 2015.03 v2.0.3 running Go 1.4"

  setting {
    namespace = "aws:elasticbeanstalk:application:environment"
    name      = "MYSQLHOST"
    value     = aws_db_instance.mysql_database.address
  }

  setting {
    namespace = "aws:elasticbeanstalk:application:environment"
    name      = "Subnets"
    value     = "subnet-xxxxxxxx"
  }
}