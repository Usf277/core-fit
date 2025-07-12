variable "aws_region" {
  description = "AWS region to deploy resources"
  type        = string
  default     = "us-east-1"
}

variable "app_name" {
  description = "Application name"
  type        = string
  default     = "core-fit"
}

variable "mysql_database" {
  description = "MySQL database name"
  type        = string
  default     = "corefit_db"
}

variable "mysql_user" {
  description = "MySQL username"
  type        = string
  default     = "corefit_user"
}

variable "mysql_password" {
  description = "MySQL password"
  type        = string
  sensitive   = true
}

variable "mysql_port" {
  description = "MySQL port"
  type        = number
  default     = 3306
}

variable "redis_port" {
  description = "Redis port"
  type        = number
  default     = 6379
}

variable "redis_password" {
  description = "Redis password"
  type        = string
  sensitive   = true
}

variable "cloudinary_cloud_name" {
  description = "Cloudinary cloud name"
  type        = string
  default     = "do75kiqcv"
}

variable "cloudinary_api_key" {
  description = "Cloudinary API key"
  type        = string
  sensitive   = true
}

variable "cloudinary_api_secret" {
  description = "Cloudinary API secret"
  type        = string
  sensitive   = true
}

variable "firebase_config" {
  description = "Firebase configuration JSON"
  type        = string
  sensitive   = true
}