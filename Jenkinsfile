pipeline {
    options {
        skipDefaultCheckout()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }
    agent { label 'd3-build-agent' }
    stages {
        stage('Tests') {
            steps {
                script {
                    checkout scm
                    docker.image("gradle")
                            .inside("-v /var/run/docker.sock:/var/run/docker.sock -v /tmp:/tmp") {
                        sh "gradle test --info"
                        sh "gradle compileIntegrationTestKotlin --info"
                        sh "gradle integrationTest --info"
                    }
                }
            }
        }
    }
}
