pipeline {
    agent any
    environment {
        	AWS_DEFAULT_REGION="eu-central-1"
        	CREDS=credentials('jenkins_s3')
    }
        parameters {
        string(name: 's3_name', defaultValue: 'dwdresume.com', description: 'Name of the s3 bucket')
        string(name: 'cypress_directory', defaultValue: 'D:\\cypress\\cypress-auto', description: 'directory of cypress, so you can use it')
        string(name: 'cypress_test', defaultValue: 'D:\\cypress\\cypress-auto\\cypress\\e2e\\hom2.cy.js', description: 'Path to Cypress test which you want to use')
        string(name: 'github_repository', defaultValue: 'https://github.com/vverrty/s3', description: 'Github repository which will be used to update S3 bucket')
        string(name: 'working_directory', defaultValue: 'C:\\Users\\werrty\\Documents\\project\\clone_test\\s3', description: 'Working directory which Jenkins will use')

    }
    stages {
        stage('Test') {
              steps {
                        bat """
                        aws s3api head-bucket --bucket ${params.s3_name}
                        if %ERRORLEVEL% == 0 (
                            echo ${params.s3_name} exists!
                            exit 0
                        ) else (
                            echo ${params.s3_name} does not exist!
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
                bat"""
                git clone ${params.github_repository} ${params.working_directory}
                """
            }
        }
        stage('Update s3') {
            steps {
                bat"""
                aws s3 sync ${params.working_directory} s3://dwdresume.com --exclude "*" --include "index.html" --include "mystyle.css"
                aws s3 ls s3://dwdresume.com
                """
            }
        }
        stage('Clean ZIP') {
            steps {
                bat """
                RMDIR /S /Q ${params.working_directory}
                """
            }
        }
    }
}