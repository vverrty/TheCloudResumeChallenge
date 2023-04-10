#configure aws provider
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.0"
    }
  }
}

provider "aws" {
  region = "" #<=== must be defined
}

#create dynamodb
resource "aws_dynamodb_table" "basic-dynamodb-table" {
  name           = "" #<=== must be defined
  billing_mode   = "PROVISIONED"
  read_capacity  = 20
  write_capacity = 20
  hash_key       = "views"
#assign attribute
attribute {
    name = "views"
    type = "S"
  }
}

#add values to database
resource "aws_dynamodb_table_item" "basic-dynamodb-table" {
  table_name = aws_dynamodb_table.basic-dynamodb-table.name
  hash_key   = aws_dynamodb_table.basic-dynamodb-table.hash_key

  item = <<ITEM
{
  "views": {"S": "views"},
  "counter": {"N": "0"}
}
ITEM
}