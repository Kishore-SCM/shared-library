// vars/buildApp.groovy
def call(Map config = [:]) {
    def serviceName = config.serviceName ?: env.SERVICE_NAME
    def language    = config.language    ?: env.LANGUAGE
 
    stage("Build: ${serviceName}") {
        switch (language.toLowerCase()) {
            case "java":
                sh "./mvnw clean package -DskipTests -q"
                break
            case "python":
                sh "pip install -r requirements.txt -q"
                break
            case "node": case "nodejs":
                sh "npm ci --silent"
                break
            case "go":
                sh "go build ./..."
                break
            default:
                error "Unsupported language: ${language}"
        }
        // Build Docker image with build number + commit SHA tag
        sh """
            docker build \
              -t ${env.DOCKER_IMAGE} \
              -t ${env.DOCKER_REPO}/${serviceName}:latest \
              .
        """
        echo "Built: ${env.DOCKER_IMAGE}"
    }
}

