output "instance_public_ip" {
  description = "Public IP address of the EC2 instance"
  value       = aws_instance.app_server.public_ip
}

output "instance_public_dns" {
  description = "Public DNS of the EC2 instance"
  value       = aws_instance.app_server.public_dns
}

output "s3_media_bucket_name" {
  description = "Name of the created S3 media bucket"
  value       = aws_s3_bucket_public_access_block.media_bucket_access.bucket
}

output "s3_media_bucket_region" {
  description = "Region of the S3 media bucket"
  value       = aws_s3_bucket.media_bucket.region
}

output "ssh_connection_command" {
  description = "Command to SSH into the instance"
  value       = "ssh -i \"${var.key_name}.pem\" ubuntu@${aws_instance.app_server.public_ip}"
}
