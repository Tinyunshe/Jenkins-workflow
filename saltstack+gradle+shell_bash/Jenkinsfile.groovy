pipeline {
    parameters {
        choice choices: ['thrid-system', 'online-system', 'test-system'], description: '选择部署环境 ', name: 'deploy_env'
        choice choices: ['master', 'trunk'], description: '选择分支', name: 'deploy_branch'
    }
    
    environment {
        PROJECT_NAME = "${params.deploy_branch}"
        PROJECT_TYPE = "order"
        PROJECT_ENV = "${params.deploy_env}"
        GITLAB_CRE = "id-name"
        GITLAB_URL = "http://gitlab.example.cn/Boo.git"
        SCRIPTS_DIR = "/data/server/jenkins/scripts_build"
    }
    
    agent { label "${params.deploy_env}" }
    
    tools {
        gradle "gradle3.5"
    }

    stages {
    
        stage('清空目录') {
            steps {
                echo "清空目录"
                deleteDir()
            }
        }

        stage('获取代码') {
            steps {
                echo "获取代码"
                git branch: "${PROJECT_NAME}", credentialsId: "${GITLAB_CRE}", url: "${GITLAB_URL}"
            }
        }

        stage('构建项目') {
            steps {
                echo "构建项目"
                sh 'gradle clean build'
            }
        }
        
        stage('部署检查') {
          parallel {
                stage('分发部署') {
                    steps {
                    sh '${SCRIPTS_DIR}/salt_deploy.sh ${PROJECT_TYPE} ${PROJECT_ENV}'
                    }
                }
                stage('检查日志') {
                    steps {
                    sh '${SCRIPTS_DIR}/checklog.sh ${PROJECT_TYPE} ${PROJECT_ENV}'
                    }
                }
            }
        }
    }
}