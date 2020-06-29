pipeline {
    agent{node('master')}
    stages {
        stage('Clean workspace & dowload dist') {
            steps {
                script {
                    cleanWs()
                    withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        try {
                            sh "echo '${password}' | sudo -S docker stop karav197"
                            sh "echo '${password}' | sudo -S docker container rm karav197"
                        } catch (Exception e) {
                            print 'container not exist, skip clean'
                        }
                    }
                }
                script {
                    echo 'Update from repository'
                    checkout([$class                           : 'GitSCM',
                              branches                         : [[name: '*/master']],
                              doGenerateSubmoduleConfigurations: false,
                              extensions                       : [[$class           : 'RelativeTargetDirectory',
                                                                   relativeTargetDir: 'auto']],
                              submoduleCfg                     : [],
                              userRemoteConfigs                : [[credentialsId: 'akarav', url: 'https://github.com/karav197/dockerExercises.git']]])
                }
            }
        }
        stage ('Build & run docker image'){
            steps{
                script{
                     withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {

                        sh "echo '${password}' | sudo -S docker build ${WORKSPACE}/auto -t karav197"
                        sh "echo '${password}' | sudo -S docker run -d -p 8981:80 --name karav197 -v /home/adminci/is_mount_dir:/stat karav197"
                    }
                }
            }
        }
        stage ('Get stats & write to file'){
            steps{
                script{
                    withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        
                        sh "echo '${password}' | sudo -S docker exec -t karav197 bash -c 'df -h > /stat/stats.txt'"
                        sh "echo '${password}' | sudo -S docker exec -t karav197 bash -c 'top -n 1 -b >> /stat/stats.txt'"
                    }
                }
            }
        }
        
    }

    
}