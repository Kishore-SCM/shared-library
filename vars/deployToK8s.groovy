// vars/deployToK8s.groovy
def call(Map config = [:]) {
    def serviceName = config.serviceName ?: env.SERVICE_NAME
    def dockerImage = config.dockerImage ?: env.DOCKER_IMAGE
    def environment = config.environment ?: "dev"
    def replicas    = environment == "production" ? 3 : 1
 
    stage("Deploy: ${serviceName} → ${environment}") {
 
        // Production requires manual approval
        if (environment == "production") {
            timeout(time: 30, unit: "MINUTES") {
                input message: "Deploy ${serviceName} to PRODUCTION?",
                      ok: "Yes, deploy!",
                      submitter: "devops-leads,release-managers"
            }
        }
 
        withCredentials([file(credentialsId: "kubeconfig-${environment}",
                              variable: "KUBECONFIG")]) {
            sh "kubectl get ns ${environment} || kubectl create ns ${environment}"
 
            sh """
                helm upgrade --install ${serviceName} charts/microservice \
                  --namespace ${environment} \
                  --set image.repository=\$(echo ${dockerImage} | cut -d: -f1) \
                  --set image.tag=\$(echo ${dockerImage} | cut -d: -f2) \
                  --set replicaCount=${replicas} \
                  --set environment=${environment} \
                  --values charts/values-${environment}.yaml \
                  --wait --timeout 5m --atomic
            """
 
            sh "kubectl rollout status deployment/${serviceName} -n ${environment} --timeout=3m"
        }
        echo "Deployed ${serviceName} to ${environment}"
    }
}

