// devops-config/seed-job/SeedJob.groovy
pipeline {
    agent any
    parameters {
        booleanParam(name: "DRY_RUN", defaultValue: false,
                     description: "Preview jobs to create without creating them")
    }
    stages {
        stage("Checkout Config") {
            steps {
                git url: "https://github.com/myorg/devops-config.git", branch: "main"
            }
        }
        stage("Create Pipelines") {
            steps {
                script {
                    def yaml    = readYaml file: "seed-job/services.yaml"
                    def global  = yaml.global
                    def services = yaml.services
 
                    echo "Found ${services.size()} services"
                    if (params.DRY_RUN) { echo "DRY RUN — no jobs created"; return }
 
                    services.each { svc ->
                        def jobName = "microservices/${svc.name}"
                        pipelineJob(jobName) {
                            description("Auto-generated | ${svc.language} | ${svc.environment}")
                            logRotator { numToKeep(10) }
                            definition {
                                scmPipeline {
                                    scm {
                                        git {
                                            remote { url(svc.repo); credentials("github-credentials") }
                                            branch(svc.branch ?: global.branch_to_deploy)
                                        }
                                    }
                                    scriptPath("Jenkinsfile")
                                }
                            }
                        }
                        echo "Created: ${jobName}"
                    }
                }
            }
        }
        stage("Trigger Initial Builds") {
            steps {
                script {
                    def yaml = readYaml file: "seed-job/services.yaml"
                    yaml.services.each { svc ->
                        build job: "microservices/${svc.name}", wait: false, propagate: false
                    }
                }
            }
        }
    }
}

