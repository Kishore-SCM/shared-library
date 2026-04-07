// shared-library/devops-config/seed-job/SeedJob.groovy

def services = [
    [
        name:        'user-service',
        repo:        'https://github.com/Kishore-SCM/shared-library.git',
        language:    'java',
        environment: 'production',
        branch:      'master'
    ],
    [
        name:        'payment-service',
        repo:        'https://github.com/Kishore-SCM/shared-library.git',
        language:    'python',
        environment: 'staging',
        branch:      'master'
    ],
    [
        name:        'order-service',
        repo:        'https://github.com/Kishore-SCM/shared-library.git',
        language:    'node',
        environment: 'staging',
        branch:      'master'
    ],
]

services.each { svc ->
    pipelineJob("microservices/${svc.name}") {
        description("Auto-generated | ${svc.language} | ${svc.environment}")

        logRotator {
            numToKeep(10)
        }

        definition {
            cpsScm {
                scm {
                    git {
                        remote {
                            url(svc.repo)
                            credentials('git')
                        }
                        branch(svc.branch)
                    }
                }
                scriptPath("${svc.name}/Jenkinsfile")
            }
        }
    }

   println "Created: microservices/${svc.name}" // ← correct for Job DSL
}

println "All pipelines created successfully!"    // ← correct
