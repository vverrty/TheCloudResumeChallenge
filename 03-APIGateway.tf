#define rest API
resource "aws_api_gateway_rest_api" "example" {
  name = "" #<=== must be defined

  endpoint_configuration {
    types = ["REGIONAL"]
  }
}
#define resource
resource "aws_api_gateway_resource" "example" {
  parent_id   = aws_api_gateway_rest_api.example.root_resource_id
  path_part   = "example_gateway_resource"
  rest_api_id = aws_api_gateway_rest_api.example.id
}
#define method
resource "aws_api_gateway_method" "example" {
  authorization = "NONE"
  http_method   = "POST"
  resource_id   = aws_api_gateway_resource.example.id
  rest_api_id   = aws_api_gateway_rest_api.example.id
}
#connect API with Lambda
resource "aws_api_gateway_integration" "example" {
  http_method = aws_api_gateway_method.example.http_method
  resource_id = aws_api_gateway_resource.example.id
  rest_api_id = aws_api_gateway_rest_api.example.id
  integration_http_method = "POST"
  type        = "AWS_PROXY"
  uri         = aws_lambda_function.lambda_terraform.invoke_arn
}
#Assign permission to API so you can trigger the lambda function
resource "aws_lambda_permission" "lambda_permission" {
  statement_id  = "AllowMyDemoAPIInvoke"
  action        = "lambda:InvokeFunction"
  function_name = "lambda_terraform"
  principal     = "apigateway.amazonaws.com"

  source_arn = "${aws_api_gateway_rest_api.example.execution_arn}/*/POST/example_gateway_resource"
}
#deploy the api gateway
resource "aws_api_gateway_deployment" "example" {
  rest_api_id = aws_api_gateway_rest_api.example.id
  stage_name = "prod"
  lifecycle {
    create_before_destroy = true
  }
}