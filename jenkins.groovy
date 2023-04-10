pipeline {
    agent any
    environment {
                AWS_DEFAULT_REGION="eu-central-1"
            	CREDS=credentials('jenkins-aws')
            }
    parameters {
        string(name: 'lambda_function_name', defaultValue: '', description: 'Name of the Lambda function, note this will be also the same name uploaded to AWS!')
        string(name: 'cypress_directory', defaultValue: '', description: 'directory of cypress, so you can use it')
        string(name: 'cypress_test', defaultValue: '', description: 'Path to Cypress test which you want to use')
        string(name: 'github_repository', defaultValue: 'https://raw.githubusercontent.com/vverrty/lambda/main/lambda.py', description: 'Github RAW file which will be used to update the AWS Lambda function')
        string(name: 'working_directory', defaultValue: '', description: 'Working directory which Jenkins will use')

    }
    stages {
        stage('Test') {
              steps {
                        bat """
                        aws lambda get-function --function-name ${params.lambda_function_name} > nul 2>&1
                        if %ERRORLEVEL% == 0 (
                            echo Lambda function exists
                            exit 0
                        ) else (
                            echo Lambda function does not exist
                            exit 1
                        )
                        """
                    dir("${params.cypress_directory}"){
                        bat """
                        npx cypress run --spec ${params.cypress_test}
                        """
                    }
                }
              }
        stage('Clone') {
            steps {
                bat """
                curl ${params.github_repository} > ${params.working_directory}\\${params.lambda_function_name}.py
                7z a ${params.working_directory}\\${params.lambda_function_name}.zip ${params.working_directory}\\${params.lambda_function_name}.py
                """
            }
        }
        stage('Update Lambda') {
            steps {
                bat"""
                aws lambda update-function-code --function-name ${params.lambda_function_name} --zip-file fileb://${params.working_directory}\\${params.lambda_function_name}.zip
                """
            }
        }
        stage('Clean ZIP') {
            steps {
                dir("${params.working_directory}"){
                    bat """
                    IF EXIST ${params.lambda_function_name}.py DEL ${params.lambda_function_name}.py
                    IF EXIST ${params.lambda_function_name}.zip DEL ${params.lambda_function_name}.zip
                    """
                }
            }
        }
    }
}
