def dockerTags = ['master': 'latest', 'develop': 'develop']
pipeline {
    options {
        buildDiscarder(logRotator(numToKeepStr: '30'))
        timestamps()
    }
    agent {
        docker {
            label 'd3-build-agent'
            image 'openjdk:8-jdk-alpine'
            args '-v /var/run/docker.sock:/var/run/docker.sock -v /tmp:/tmp'
        }
    }
    stages {
        stage('Test') {
            steps {
                script {
                    sh "./gradlew clean test --info"
                }
            }
        }
        stage('Build') {
            steps {
                script {
                    sh "./gradlew clean build --info"
                }
            }
        }
        stage('Push artifacts') {
            when {
              expression { return (env.GIT_BRANCH in dockerTags || env.TAG_NAME) }
            }
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'bot-d3-rw', usernameVariable: 'DOCKER_REGISTRY_USERNAME', passwordVariable: 'DOCKER_REGISTRY_PASSWORD')]) {
                        env.DOCKER_REGISTRY_URL = "https://docker.soramitsu.co.jp"
                        env.TAG = env.TAG_NAME ? env.TAG_NAME : dockerTags[env.GIT_BRANCH]
                        sh "./gradlew dockerPush"
                    }
                }
            }
        }
    }
    post {
        always {
            publishHTML (target: [
                allowMissing: false,
                alwaysLinkToLastBuild: false,
                keepAll: true,
                reportDir: 'build/reports',
                reportFiles: 'd3-test-report.html',
                reportName: "D3 test report"
            ])
        }
        cleanup {
            cleanWs()
        }
    }
}
