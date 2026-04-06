// vars/standardPipeline.groovy
def call(Map config = [:]) {
    def serviceName    = config.serviceName                  // REQUIRED
    def language       = config.language                     // REQUIRED
    def environment    = config.environment    ?: "dev"
    def dockerRepo     = config.dockerRepo     ?: "mycompany"
    def slackChannel   = config.slackChannel   ?: "#deployments"
    def runSecScan     = config.runSecScan     ?: true
    def branchToDeploy = config.branchToDeploy ?: "main"
 
    if (!serviceName || !language) {
        error "standardPipeline requires serviceName and language"
    }
 
    env.SERVICE_NAME = serviceName
    env.LANGUAGE     = language
    env.DOCKER_REPO  = dockerRepo
 
    pipeline {
        agent { label "docker-agent" }
        options {
            timeout(time: 45, unit: "MINUTES")
            disableConcurrentBuilds()
            buildDiscarder(logRotator(numToKeepStr: "10"))
            timestamps()
        }
 
        stages {
            stage("Checkout") { steps { script {
                checkout scm
                env.GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD",
                                          returnStdout: true).trim()
                env.DOCKER_IMAGE = "${dockerRepo}/${serviceName}:${env.BUILD_NUMBER}-${env.GIT_COMMIT_SHORT}"
            }}}
 
            stage("Build")          { steps { script { buildApp(config) }}}
            stage("Test")           { steps { script { runTests(config) }}}
            stage("Security Scan")  { when { expression { runSecScan } }
                                      steps { script { securityScan(config) }}}
            stage("Push Image")     { when { branch branchToDeploy }
                                      steps { script { pushImage(config) }}}
            stage("Deploy")         { when { branch branchToDeploy }
                                      steps { script { deployToK8s(config) }}}
        }
        post {
            success  { script { notify(status: "SUCCESS",  ...config) }}
            failure  { script { notify(status: "FAILURE",  ...config) }}
            unstable { script { notify(status: "UNSTABLE", ...config) }}
            always   { cleanWs() }
        }
    }
}

