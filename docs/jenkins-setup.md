# Jenkins CI/CD 설정 가이드

## 1. Jenkins 설치 및 초기 설정

### Docker로 Jenkins 설치
```bash
# Jenkins 컨테이너 실행
docker run -d \
  --name jenkins \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts

# 초기 관리자 비밀번호 확인
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### 필수 플러그인 설치
Jenkins 관리 → 플러그인 관리에서 다음 플러그인들을 설치:

1. **Git Plugin** - Git 저장소 연동
2. **Docker Pipeline Plugin** - Docker 빌드 지원
3. **Pipeline Plugin** - 파이프라인 기능
4. **Blue Ocean** - 현대적인 UI
5. **SonarQube Scanner** - 코드 품질 분석
6. **Slack Notification** - 슬랙 알림
7. **SSH Agent** - SSH 키 관리
8. **Credentials Plugin** - 인증 정보 관리
9. **Build Timeout** - 빌드 타임아웃 설정
10. **Timestamper** - 로그에 타임스탬프 추가

## 2. Credentials 설정

Jenkins 관리 → Credentials → System → Global credentials에서 다음 인증 정보들을 추가:

### 2.1 Docker Hub 인증
- **Kind**: Username with password
- **ID**: `docker-hub-credentials`
- **Username**: Docker Hub 사용자명
- **Password**: Docker Hub 비밀번호

### 2.2 Docker Registry
- **Kind**: Secret text
- **ID**: `docker-registry`
- **Secret**: Docker Registry URL (예: `registry.hub.docker.com`)

### 2.3 데이터베이스 비밀번호
- **Kind**: Secret text
- **ID**: `db-password`
- **Secret**: 데이터베이스 비밀번호

- **Kind**: Secret text
- **ID**: `db-root-password`
- **Secret**: 데이터베이스 루트 비밀번호

### 2.4 JWT Secret
- **Kind**: Secret text
- **ID**: `jwt-secret`
- **Secret**: JWT 시크릿 키 (최소 32자)

### 2.5 서버 정보
- **Kind**: Secret text
- **ID**: `prod-server`
- **Secret**: 프로덕션 서버 주소

- **Kind**: Secret text
- **ID**: `staging-server`
- **Secret**: 스테이징 서버 주소

### 2.6 SSH 키
- **Kind**: SSH Username with private key
- **ID**: `prod-ssh-key`
- **Username**: 서버 사용자명
- **Private Key**: 프로덕션 서버 SSH 개인키

- **Kind**: SSH Username with private key
- **ID**: `staging-ssh-key`
- **Username**: 서버 사용자명
- **Private Key**: 스테이징 서버 SSH 개인키

## 3. Pipeline Job 생성

### 3.1 새 Item 생성
1. Jenkins 대시보드에서 "새로운 Item" 클릭
2. Item 이름: `CoffeePlz-Pipeline`
3. "Pipeline" 선택 후 OK

### 3.2 Pipeline 설정
**Pipeline 섹션에서:**
- **Definition**: Pipeline script from SCM
- **SCM**: Git
- **Repository URL**: GitHub 저장소 URL
- **Credentials**: GitHub 인증 정보 (필요시)
- **Branch Specifier**: `*/main` (또는 원하는 브랜치)
- **Script Path**: `Jenkinsfile`

### 3.3 Build Triggers 설정
- **GitHub hook trigger for GITScm polling** 체크
- **Poll SCM** 체크하고 스케줄: `H/5 * * * *` (5분마다 폴링)

## 4. SonarQube 설정 (선택사항)

### 4.1 SonarQube 서버 실행
```bash
docker run -d \
  --name sonarqube \
  -p 9000:9000 \
  sonarqube:community
```

### 4.2 Jenkins에서 SonarQube 설정
1. Jenkins 관리 → 시스템 설정
2. SonarQube servers 섹션에서:
   - **Name**: `SonarQube`
   - **Server URL**: `http://localhost:9000`
   - **Server authentication token**: SonarQube에서 생성한 토큰

### 4.3 SonarQube Scanner 설정
1. Jenkins 관리 → Global Tool Configuration
2. SonarQube Scanner 섹션에서:
   - **Name**: `SonarQube Scanner`
   - **Install automatically** 체크

## 5. Slack 알림 설정 (선택사항)

### 5.1 Slack App 생성
1. Slack에서 새 앱 생성
2. Incoming Webhooks 활성화
3. Webhook URL 복사

### 5.2 Jenkins Slack 설정
1. Jenkins 관리 → 시스템 설정
2. Slack 섹션에서:
   - **Workspace**: Slack 워크스페이스명
   - **Credential**: Slack 토큰 (Secret text로 추가)
   - **Default channel**: `#deployments`

## 6. 환경별 배포 서버 설정

### 6.1 서버 준비
각 환경 서버에서 다음 작업 수행:

```bash
# Docker 설치
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Docker Compose 설치
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 배포 디렉토리 생성
sudo mkdir -p /opt/coffeeplz
sudo chown $USER:$USER /opt/coffeeplz

# 프로젝트 파일 복사
cd /opt/coffeeplz
git clone <repository-url> .
```

### 6.2 환경변수 파일 설정
각 서버에서 환경에 맞는 `.env` 파일 생성:

```bash
# 프로덕션 서버
cp config/environment-template.env .env.prod
vi .env.prod  # 프로덕션 값으로 수정

# 스테이징 서버
cp config/environment-template.env .env.staging
vi .env.staging  # 스테이징 값으로 수정
```

## 7. GitHub Webhook 설정

### 7.1 GitHub 저장소 설정
1. GitHub 저장소 → Settings → Webhooks
2. "Add webhook" 클릭
3. **Payload URL**: `http://your-jenkins-url/github-webhook/`
4. **Content type**: `application/json`
5. **Which events**: "Just the push event"
6. **Active** 체크

## 8. 파이프라인 테스트

### 8.1 첫 번째 빌드 실행
1. Jenkins에서 CoffeePlz-Pipeline 프로젝트 선택
2. "Build Now" 클릭
3. 빌드 로그 확인

### 8.2 자동 빌드 테스트
1. 코드 변경 후 GitHub에 푸시
2. Jenkins에서 자동으로 빌드가 시작되는지 확인

## 9. 모니터링 및 알림

### 9.1 빌드 상태 모니터링
- Jenkins 대시보드에서 빌드 상태 확인
- Blue Ocean UI로 파이프라인 시각화

### 9.2 로그 확인
```bash
# Jenkins 로그
docker logs jenkins

# 애플리케이션 로그
docker-compose logs -f

# 특정 서비스 로그
docker-compose logs app-prod
```

## 10. 트러블슈팅

### 10.1 일반적인 문제들

#### Docker 권한 문제
```bash
# Jenkins 사용자를 docker 그룹에 추가
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

#### Git 인증 문제
- GitHub Personal Access Token 사용
- SSH 키 설정 확인

#### 네트워크 연결 문제
```bash
# 방화벽 설정 확인
sudo ufw status
sudo ufw allow 8080  # Jenkins
sudo ufw allow 22    # SSH
```

### 10.2 성능 최적화

#### Jenkins JVM 설정
```bash
# Jenkins 시작 시 JVM 옵션 추가
-Xmx2g -Xms1g -XX:+UseG1GC
```

#### 빌드 캐시 활용
- Docker 레이어 캐싱
- Gradle 빌드 캐시
- 의존성 캐싱

## 11. 보안 설정

### 11.1 Jenkins 보안
- 관리자 계정 비밀번호 강화
- CSRF 보호 활성화
- 불필요한 플러그인 제거

### 11.2 네트워크 보안
- Jenkins 포트 방화벽 설정
- SSH 키 기반 인증 사용
- SSL/TLS 인증서 적용

이 가이드를 따라 설정하면 완전한 CI/CD 파이프라인이 구축됩니다! 