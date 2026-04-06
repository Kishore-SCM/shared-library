// vars/notify.groovy
def call(Map config = [:]) {
    def serviceName = config.serviceName ?: env.SERVICE_NAME
    def environment = config.environment ?: "dev"
    def status      = config.status      ?: currentBuild.result ?: "SUCCESS"
    def channel     = config.slackChannel ?: "#deployments"
 
    def emoji = status == "SUCCESS" ? ":white_check_mark:" : ":x:"
    def color = status == "SUCCESS" ? "good" : "danger"
 
    slackSend(
        channel: channel,
        color:   color,
        message: "${emoji} *${serviceName}* | ${status} | Env: ${environment} | <${env.BUILD_URL}|Build #${env.BUILD_NUMBER}>"
    )
 
    if (status == "FAILURE") {
        emailext(
            to:      "devops@company.com",
            subject: "Pipeline Failed: ${serviceName} #${env.BUILD_NUMBER}",
            body:    "<h2>Build Failed</h2><p>${serviceName} in ${environment}</p>"
        )
    }
}

