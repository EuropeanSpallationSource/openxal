pipeline {
    agent {
        docker {
            image 'europeanspallationsource/oracle-jdk-maven-jenkins:8'
            label 'docker'
        }
    }

    environment {
        GIT_TAG = sh(returnStdout: true, script: 'git describe --exact-match || true').trim()
    }

    stages {
        stage('Build') {
            steps {
                timestamps {
                    sh 'mvn -Dmaven.test.failure.ignore clean install'
                }
            }
        }
        stage('SonarQube analysis') {
            steps {
                timestamps {
                    sh 'mvn sonar:sonar'
                }
            }
        }
        stage('Publish') {
            when {
                not { environment name: 'GIT_TAG', value: '' }
            }
            steps {
                timestamps {
                    sh 'mvn deploy'
                }
            }
        }
    }
    post {
        failure {
            slackSend (color: 'danger', message: "FAILED: <${env.BUILD_URL}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>")
            slackSend (color: 'danger', message: "FAILED: <${env.BUILD_URL}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>", channel: "#physicsapplications")
            step([$class: 'Mailer',
                    recipients: 'emanuele.laface@esss.se; yngve.levinsen@esss.se; JuanF.EstebanMuller@esss.se',
                    sendToIndividuals: true])
        }
        success {
            slackSend (color: 'good', message: "SUCCESSFUL: <${env.BUILD_URL}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>")
            slackSend (color: 'good', message: "SUCCESSFUL: <${env.BUILD_URL}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>", channel: "#physicsapplications")
        }
    }
}

// vim: et ts=4 sw=4 :
