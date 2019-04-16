
pipeline {
  environment {
    DOCKER_NETWORK = ''
  }
  options {
    skipDefaultCheckout()
    buildDiscarder(logRotator(numToKeepStr: '20'))
    timestamps()
  }
  agent any
    stage('Tests') {
      agent { label 'd3-build-agent' }
      steps {
        script {
          def scmVars = checkout scm
          tmp = docker.image("openjdk:8-jdk")
          env.WORKSPACE = pwd()

          DOCKER_NETWORK = "${scmVars.CHANGE_ID}-${scmVars.GIT_COMMIT}-${BUILD_NUMBER}"
          writeFile file: ".env", text: "SUBNET=${DOCKER_NETWORK}"

          iC = docker.image("openjdk:8-jdk")
          iC.inside("--network='d3-${DOCKER_NETWORK}' -e JVM_OPTS='-Xmx3200m' -e TERM='dumb'") {
            sh "./gradlew test --info"
            sh "./gradlew compileIntegrationTestKotlin --info"
            sh "./gradlew integrationTest --info"
          }
      }
  }
}
