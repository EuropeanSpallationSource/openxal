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
                    sh 'mvn -DskipTests -Dmaven.javadoc.skip=true -Dmaven.test.failure.ignore clean install'
                }
            }
        }
        stage('Test') {
            steps {
                timestamps {
                    sh 'mvn test'
                }
            }
            post {
                always {
                  junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        stage('SonarQube analysis') {
            steps {
                timestamps {
                    withCredentials([string(credentialsId: 'sonarqube', variable: 'TOKEN')]) {
                        sh 'mvn -Dsonar.login=${TOKEN} sonar:sonar'
                    }
                }
            }
        }
        stage('Publish') {
            steps {
                timestamps {
                    withCredentials([usernamePassword(credentialsId: 'artifactory', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh 'mvn deploy -pl dist -Dartifactory.username=${USERNAME} -Dartifactory.password=${PASSWORD}'
                    }
                }
            }
        }
        stage('Generate JavaDoc') {
            environment {
                OXALDOC_GIT_PATH = 'oxal_documentation-deploy'
                OXALDOC_TOP_DIR = '${PWD}' + "/${OXALDOC_GIT_PATH}"
                OXALDOC_DEPLOY_DIR = 'apidoc/openxal'
                DOC_BUILD_TARGET = '${PWD}/target/site/apidocs'
                OXALDOC_GITURL = 'https://bitbucket.org/europeanspallationsource/europeanspallationsource.bitbucket.org.git'
                OXALDOC_GITUSER = 'benjaminbertrand-noemail-bitbucket'
                OXALDOC_COMMIT_MSG = "Automatic Commit: Added documentation from latest OpenXAL master branch build ${BUILD_DISPLAY_NAME}."
            }
            /*
            when {
                not { environment name: 'GIT_TAG', value: '' }
            }
            */
            steps {
                timestamps {
                    // Create javadoc target directory as a softlink to actual destination
                    sh "if [ -L ${DOC_BUILD_TARGET} ]; then rm ${DOC_BUILD_TARGET}; elif [ -e ${DOC_BUILD_TARGET} ]; then rm -rf ${DOC_BUILD_TARGET}; fi"
                    sh "mkdir -p \$(dirname ${DOC_BUILD_TARGET}) && ln -s ${OXALDOC_TOP_DIR}/${OXALDOC_DEPLOY_DIR} ${DOC_BUILD_TARGET}"
                    dir ("${OXALDOC_GIT_PATH}") {
                        git url: OXALDOC_GITURL, credentialsId: OXALDOC_GITUSER
                    }
                    // dir('foo') inside "docker.image().inside{}" does not affect CWD of launched sh processes
                    // https://issues.jenkins-ci.org/browse/JENKINS-33510
                    // Delete everything in prior javadoc release root
                    sh "cd ${OXALDOC_TOP_DIR} && git rm -qrf -- \"${OXALDOC_DEPLOY_DIR}\" && mkdir -p ${OXALDOC_DEPLOY_DIR}"
                    sh 'mvn javadoc:aggregate'
                    sh "cd ${OXALDOC_TOP_DIR}/${OXALDOC_DEPLOY_DIR} && git add . && git commit -m '${OXALDOC_COMMIT_MSG}'"
                    // https://issues.jenkins-ci.org/browse/JENKINS-28335 GitPublisher not possible to use
                    withCredentials([usernamePassword(credentialsId: OXALDOC_GITUSER, usernameVariable: 'OXALDOC_GITUSER_USR', passwordVariable: 'OXALDOC_GITUSER_PSW')]) {
                        sh "cd ${OXALDOC_TOP_DIR} && git push https://${OXALDOC_GITUSER_USR}:${OXALDOC_GITUSER_PSW}@bitbucket.org/europeanspallationsource/europeanspallationsource.bitbucket.org.git HEAD:refs/heads/master"
                    }
                }
            }
        }
    }
    post {
        failure {
            slackSend (color: 'danger', message: "FAILED: <${env.BUILD_URL}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>")
            slackSend (color: 'danger', message: "FAILED: <${env.BUILD_URL}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>", channel: "#physicsapplications")
            step([$class: 'Mailer',
                    recipients: 'emanuele.laface@esss.se, yngve.levinsen@esss.se, JuanF.EstebanMuller@esss.se',
                    sendToIndividuals: true])
        }
        success {
            slackSend (color: 'good', message: "SUCCESSFUL: <${env.BUILD_URL}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>")
            slackSend (color: 'good', message: "SUCCESSFUL: <${env.BUILD_URL}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>", channel: "#physicsapplications")
        }
    }
}

// vim: et ts=4 sw=4 syntax=groovy :
