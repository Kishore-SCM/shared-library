// vars/runTests.groovy
def call(Map config = [:]) {
    def language    = config.language    ?: env.LANGUAGE
    def serviceName = config.serviceName ?: env.SERVICE_NAME
 
    stage("Test: ${serviceName}") {
        try {
            switch (language.toLowerCase()) {
                case "java":
                    sh "./mvnw test -q"
                    junit "**/target/surefire-reports/*.xml"
                    jacoco(execPattern: "**/target/*.exec")
                    break
                case "python":
                    sh "pytest tests/ --junitxml=test-results.xml --cov=. --cov-report=xml -q"
                    junit "test-results.xml"
                    break
                case "node": case "nodejs":
                    sh "npm test -- --ci --forceExit --reporters=jest-junit"
                    junit "junit.xml"
                    break
                case "go":
                    sh "go test ./... -v 2>&1 | tee test-output.txt"
                    break
            }
        } catch (err) {
            currentBuild.result = "UNSTABLE"
            throw err
        } finally {
            archiveArtifacts artifacts: "**/test-results.xml,**/coverage.xml",
                             allowEmptyArchive: true
        }
    }
}

