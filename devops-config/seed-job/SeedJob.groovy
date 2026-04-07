// SeedJob.groovy  — pure Job DSL, no pipeline{} wrapper

import groovy.yaml.YamlSlurper   // NOT needed if using readFileFromWorkspace

def configText = readFileFromWorkspace('seed-job/services.yaml')
def config     = new groovy.yaml.YamlSlurper().parseText(configText)
def services   = config.services
def global     = config.global

services.each { svc ->
    def jobName        = "microservices/${svc.name}"
    def branchToDeploy = svc.branch ?: global.branch_to_deploy ?: 'master'
    def slackChannel   = svc.slack_channel ?: global.slack_channel ?: '#deployments'

    pipelineJob(jobName) {
        description("Auto-generated | ${svc.language} | ${svc.environment}")

        logRotator {
            numToKeep(10)
            artifactNumToKeep(5)
        }

        definition {
            cpsScm {
                scm {
                    git {
                        remote {
                            url(svc.repo)
                            credentials('git')
                        }
                        branch(branchToDeploy)
                    }
                }
                scriptPath('Jenkinsfile')
            }
        }
    }

    echo "Created pipeline: ${jobName}"
}

echo "All pipelines created successfully!"
