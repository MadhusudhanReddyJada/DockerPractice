pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                sh 'mkdir -p build && echo "Hello artifact" > build/output.txt'
            }
        }

        stage('Test') {
            steps {
                echo 'Fake tests are running...'
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'build/**', fingerprint: true
        }
    }
}
