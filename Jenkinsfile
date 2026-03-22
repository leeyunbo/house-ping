pipeline {
    agent any

    environment {
        PROJECT_DIR = '/host-project/houseping'
        SLACK_WEBHOOK = credentials('slack-webhook')
    }

    triggers {
        pollSCM('H/5 * * * *')
    }

    stages {
        stage('Pull') {
            steps {
                dir("${PROJECT_DIR}") {
                    sh 'git pull origin main'
                }
            }
        }

        stage('Build') {
            steps {
                dir("${PROJECT_DIR}") {
                    sh './gradlew clean build -x test'
                }
            }
        }

        stage('Test') {
            steps {
                dir("${PROJECT_DIR}") {
                    sh './gradlew test'
                }
            }
        }

        stage('Deploy') {
            steps {
                dir("${PROJECT_DIR}") {
                    sh 'docker build -t houseping:latest .'
                    sh 'docker stop houseping-app || true'
                    sh 'docker rm houseping-app || true'
                    sh 'docker run -d --name houseping-app -p 10030:10030 --env-file .env -e SPRING_PROFILE=local -e DB_HOST=host.docker.internal houseping:latest'
                }
            }
        }
    }

    post {
        success {
            sh """
                curl -s -X POST ${SLACK_WEBHOOK} \
                    -H 'Content-Type: application/json' \
                    -d '{"text": "\\u2705 houseping \\ubc30\\ud3ec \\uc131\\uacf5 (#${BUILD_NUMBER})"}'
            """
        }
        failure {
            sh """
                curl -s -X POST ${SLACK_WEBHOOK} \
                    -H 'Content-Type: application/json' \
                    -d '{"text": "\\u274c houseping \\ubc30\\ud3ec \\uc2e4\\ud328 (#${BUILD_NUMBER}) - ${BUILD_URL}"}'
            """
        }
    }
}
