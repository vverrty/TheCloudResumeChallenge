#Generates an IAM policy document in JSON format
data "aws_iam_policy_document" "assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}
#define initial iam role
resource "aws_iam_role" "iam_for_lambda" {
  name               = "iam_for_lambda"
  assume_role_policy = data.aws_iam_policy_document.assume_role.json
}
#attaches a Managed IAM policy to an IAM role
resource "aws_iam_role_policy_attachment" "lambda" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
  role       = aws_iam_role.iam_for_lambda.name
}
#attaches a Managed IAM policy to an IAM role
resource "aws_iam_role_policy_attachment" "dynamodb_full_access" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess"
  role       = aws_iam_role.iam_for_lambda.name
}
#Creates the lambda function from a zip file
resource "aws_lambda_function" "lambda_terraform" {
  filename      = "" #<=== must be defined
  function_name = "" #<=== must be defined
  role          = aws_iam_role.iam_for_lambda.arn
  handler       = "lambda_terraform.lambda_handler"
  runtime       = "python3.8"
}