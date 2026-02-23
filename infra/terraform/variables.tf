variable "aws_region" {
  description = "AWS region for deployment"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Base name for project resources"
  type        = string
  default     = "secure-math"
}

variable "environment" {
  description = "Deployment environment (e.g., prod, staging, dev)"
  type        = string
  default     = "prod"
}

variable "instance_type" {
  description = "EC2 instance type (t3.large is recommended to support PostgreSQL, Java, and Video processing for 500 concurrent users)"
  type        = string
  default     = "t3.small"
}

variable "volume_size" {
  description = "Root EBS volume size in GB"
  type        = number
  default     = 50
}

variable "key_name" {
  description = "Name of an existing AWS Key Pair to allow SSH access to the EC2 instance"
  type        = string
  default     = "sezar-drive" # Provide your key pair name here or via tfvars
}

variable "allowed_ssh_cidr" {
  description = "CIDR block allowed to SSH into the EC2 instance"
  type        = string
  default     = "102.43.162.240/32" # Update to your specific IP (e.g., '192.168.1.1/32') for better security
}
