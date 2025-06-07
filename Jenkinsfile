pipeline {
    agent any
    
    environment {
        // Docker 관련 환경변수
        DOCKER_REGISTRY = credentials('docker-registry')
        DOCKER_CREDENTIALS = credentials('docker-hub-credentials')
        IMAGE_NAME = 'coffeeplz'
        IMAGE_TAG = "${BUILD_NUMBER}"
        
        // 애플리케이션 환경변수
        DB_PASSWORD = credentials('db-password')
        DB_ROOT_PASSWORD = credentials('db-root-password')
        JWT_SECRET = credentials('jwt-secret')
        
        // 배포 환경변수
        PROD_SERVER = credentials('prod-server')
        STAGING_SERVER = credentials('staging-server')
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
                script {
                    // Git 정보 설정
                    env.GIT_COMMIT = sh(
                        script: 'git rev-parse HEAD',
                        returnStdout: true
                    ).trim()
                    env.GIT_BRANCH = sh(
                        script: 'git rev-parse --abbrev-ref HEAD',
                        returnStdout: true
                    ).trim()
                }
            }
        }
        
        stage('Build') {
            steps {
                echo 'Building application...'
                sh 'chmod +x gradlew'
                sh './gradlew clean build -x test'
            }
            post {
                always {
                    // 빌드 아티팩트 보관
                    archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
                }
            }
        }
        
        stage('Unit Tests') {
            steps {
                echo 'Running unit tests...'
                sh './gradlew test'
            }
            post {
                always {
                    // 테스트 결과 발행
                    publishTestResults testResultsPattern: 'build/test-results/test/*.xml'
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'build/reports/tests/test',
                        reportFiles: 'index.html',
                        reportName: 'Unit Test Report'
                    ])
                }
            }
        }
        
        stage('Code Quality Analysis') {
            parallel {
                stage('SonarQube Analysis') {
                    when {
                        anyOf {
                            branch 'main'
                            branch 'develop'
                        }
                    }
                    steps {
                        script {
                            def scannerHome = tool 'SonarQube Scanner'
                            withSonarQubeEnv('SonarQube') {
                                sh "${scannerHome}/bin/sonar-scanner"
                            }
                        }
                    }
                }
                
                stage('Security Scan') {
                    steps {
                        echo 'Running security scan...'
                        sh './gradlew dependencyCheckAnalyze'
                    }
                    post {
                        always {
                            publishHTML([
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'build/reports',
                                reportFiles: 'dependency-check-report.html',
                                reportName: 'Security Scan Report'
                            ])
                        }
                    }
                }
            }
        }
        
        stage('Docker Build & Test') {
            steps {
                echo 'Building Docker image...'
                script {
                    // Docker 이미지 빌드
                    def image = docker.build("${IMAGE_NAME}:${IMAGE_TAG}")
                    
                    // 컨테이너 테스트
                    echo 'Running container tests...'
                    sh 'docker-compose -f docker-compose.test.yml up --build --abort-on-container-exit'
                    
                    // 이미지 태깅
                    image.tag('latest')
                    if (env.GIT_BRANCH == 'main') {
                        image.tag('stable')
                    }
                }
            }
            post {
                always {
                    // 테스트 컨테이너 정리
                    sh 'docker-compose -f docker-compose.test.yml down -v'
                }
            }
        }
        
        stage('Integration Tests') {
            steps {
                echo 'Running integration tests...'
                sh 'docker-compose -f docker-compose.test.yml up -d mysql-test'
                sh 'sleep 30' // DB 초기화 대기
                sh './gradlew integrationTest'
            }
            post {
                always {
                    sh 'docker-compose -f docker-compose.test.yml down'
                    publishTestResults testResultsPattern: 'build/test-results/integrationTest/*.xml'
                }
            }
        }
        
        stage('Push to Registry') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                echo 'Pushing Docker image to registry...'
                script {
                    docker.withRegistry("https://${DOCKER_REGISTRY}", DOCKER_CREDENTIALS) {
                        def image = docker.image("${IMAGE_NAME}:${IMAGE_TAG}")
                        image.push()
                        image.push('latest')
                        
                        if (env.GIT_BRANCH == 'main') {
                            image.push('stable')
                        }
                    }
                }
            }
        }
        
        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                echo 'Deploying to staging environment...'
                script {
                    sshagent(['staging-ssh-key']) {
                        sh """
                            ssh -o StrictHostKeyChecking=no ${STAGING_SERVER} '
                                cd /opt/coffeeplz &&
                                docker-compose -f docker-compose.staging.yml pull &&
                                docker-compose -f docker-compose.staging.yml up -d &&
                                docker system prune -f
                            '
                        """
                    }
                }
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                echo 'Deploying to production environment...'
                script {
                    // 배포 승인 단계
                    timeout(time: 5, unit: 'MINUTES') {
                        input message: 'Deploy to production?', ok: 'Deploy',
                              submitterParameter: 'DEPLOYER'
                    }
                    
                    sshagent(['prod-ssh-key']) {
                        sh """
                            ssh -o StrictHostKeyChecking=no ${PROD_SERVER} '
                                cd /opt/coffeeplz &&
                                export IMAGE_TAG=${IMAGE_TAG} &&
                                docker-compose -f docker-compose.prod.yml pull &&
                                docker-compose -f docker-compose.prod.yml up -d &&
                                docker system prune -f
                            '
                        """
                    }
                }
            }
        }
        
        stage('Health Check') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                echo 'Performing health check...'
                script {
                    def server = env.GIT_BRANCH == 'main' ? PROD_SERVER : STAGING_SERVER
                    def port = env.GIT_BRANCH == 'main' ? '80' : '8080'
                    
                    // 헬스체크 실행
                    timeout(time: 5, unit: 'MINUTES') {
                        sh """
                            for i in {1..30}; do
                                if curl -f http://${server}:${port}/actuator/health; then
                                    echo "Health check passed"
                                    exit 0
                                fi
                                echo "Waiting for application to start..."
                                sleep 10
                            done
                            echo "Health check failed"
                            exit 1
                        """
                    }
                }
            }
        }
    }
    
    post {
        always {
            // 워크스페이스 정리
            cleanWs()
        }
        
        success {
            echo 'Pipeline completed successfully!'
            // 성공 알림 (Slack, 이메일 등)
            slackSend(
                channel: '#deployments',
                color: 'good',
                message: """
                    ✅ CoffeePlz Deployment Success
                    Branch: ${env.GIT_BRANCH}
                    Build: ${env.BUILD_NUMBER}
                    Commit: ${env.GIT_COMMIT}
                    Deployer: ${env.DEPLOYER ?: 'Auto'}
                """
            )
        }
        
        failure {
            echo 'Pipeline failed!'
            // 실패 알림
            slackSend(
                channel: '#deployments',
                color: 'danger',
                message: """
                    ❌ CoffeePlz Deployment Failed
                    Branch: ${env.GIT_BRANCH}
                    Build: ${env.BUILD_NUMBER}
                    Commit: ${env.GIT_COMMIT}
                    Check: ${env.BUILD_URL}
                """
            )
        }
        
        unstable {
            echo 'Pipeline is unstable!'
            slackSend(
                channel: '#deployments',
                color: 'warning',
                message: """
                    ⚠️ CoffeePlz Deployment Unstable
                    Branch: ${env.GIT_BRANCH}
                    Build: ${env.BUILD_NUMBER}
                    Commit: ${env.GIT_COMMIT}
                """
            )
        }
    }
} 