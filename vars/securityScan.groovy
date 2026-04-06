// vars/securityScan.groovy
def call(Map config = [:]) {
    def serviceName = config.serviceName ?: env.SERVICE_NAME
    def dockerImage = config.dockerImage ?: env.DOCKER_IMAGE
 
    stage("Security Scan: ${serviceName}") {
        parallel(
            "SAST (Semgrep)": {
                sh """
                    semgrep --config=auto --json --output=semgrep-report.json . || true
                """
                archiveArtifacts "semgrep-report.json"
            },
            "Container Scan (Trivy)": {
                sh "trivy image --format json --output trivy-report.json ${dockerImage} || true"
                def report      = readJSON file: "trivy-report.json"
                def highCount   = report?.Results?.sum {
                    it.Vulnerabilities?.count { v ->
                        v.Severity in ["HIGH", "CRITICAL"] } ?: 0 } ?: 0
                if (highCount > 0) {
                    error "${highCount} HIGH/CRITICAL CVEs found in ${serviceName}!"
                }
                archiveArtifacts "trivy-report.json"
            }
        )
    }
}

