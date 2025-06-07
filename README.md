# CoffeePlz ☕

QR코드 기반 카페 테이블 주문 시스템 백엔드 API

## 📋 프로젝트 소개

Spring Boot 기반의 **QR코드 테이블 주문 시스템**입니다. 카페 테이블에 QR코드를 배치하여 고객이 직접 스캔하고 주문할 수 있으며, 관리자는 테이블 도식화된 대시보드에서 실시간 주문 관리와 매출 분석을 할 수 있는 현대적인 카페 운영 솔루션을 제공합니다.

## 🎯 **시스템 플로우**

### **🛒 소비자 플로우 (익명 주문)**
1. **🚶‍♂️ 가게 방문** → 테이블 착석
2. **📱 QR 코드 촬영** → 테이블 식별 및 메뉴판 접근
3. **🍽️ 메뉴 선택** → 원하는 메뉴 및 옵션 선택
4. **🛒 장바구니 확인** → 주문 내역 검토 및 수정
5. **💳 간편결제** → 카드/모바일 결제로 주문 완료

### **👨‍💼 관리자 플로우**
1. **🔐 회원가입/로그인** → 관리자 인증
2. **📊 테이블 도식화 메인페이지** → 전체 테이블 현황 실시간 조회
3. **💰 매출 대시보드** → 일매출, 주매출, 월매출 확인
4. **🏪 테이블 관리** → 테이블 번호 및 도식화 변경
5. **📋 메뉴판 관리** → 메뉴/사진 등록, 변경, 추가, 삭제
6. **🍽️ 주문 처리** → 각 테이블 클릭으로 서빙 상태 관리 (준비중→완료→서빙완료)

### 🌟 **핵심 특징**
- **소비자**: 회원가입 없는 빠른 QR 주문 시스템
- **관리자**: 테이블 도식화 기반 직관적 주문 관리
- **실시간**: 매출 분석 및 서빙 상태 관리

## 🛠 기술 스택

### Backend
- **Java 17**
- **Spring Boot 3.2.1**
- **Spring Data JPA**
- **Spring Security**
- **JWT**
- **BCrypt**

### Database
- **MySQL** (운영환경)
- **H2** (개발/테스트환경)

### 문서화 & 테스트
- **Swagger (SpringDoc)**
- **JUnit 5**
- **Mockito**

### 배포 & 인프라
- **Docker**
- **Docker Compose**
- **Jenkins**

## 🏗 프로젝트 구조

```
src/
├── main/
│   ├── java/com/coffeeplz/
│   │   ├── config/          # 설정 클래스
│   │   ├── controller/      # REST API 컨트롤러
│   │   │   ├── table/       # QR 테이블 주문 API
│   │   │   ├── menu/        # 메뉴 관리 API  
│   │   │   ├── order/       # 주문 관리 API
│   │   │   └── admin/       # 관리자 API
│   │   ├── service/         # 비즈니스 로직
│   │   │   ├── table/       # 테이블 관리 서비스
│   │   │   ├── qr/          # QR코드 관리 서비스
│   │   │   └── order/       # 주문 처리 서비스
│   │   ├── repository/      # 데이터 접근 계층
│   │   ├── entity/          # JPA 엔티티
│   │   │   ├── Table.java   # 테이블 엔티티
│   │   │   ├── TableStatus.java # 테이블 상태 enum
│   │   │   └── ...          # 기타 엔티티
│   │   ├── dto/             # 데이터 전송 객체
│   │   │   ├── table/       # 테이블 관련 DTO
│   │   │   └── order/       # 주문 관련 DTO
│   │   ├── exception/       # 예외 처리
│   │   └── security/        # 보안 설정
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
└── test/                    # 테스트 코드
```

## 🚀 실행 방법

### 개발환경 (H2 DB)
```bash
# H2 데이터베이스로 실행
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 운영환경 (Docker + MySQL)
```bash
# Docker Compose로 실행
docker-compose up --build
```

### API 문서
- **Swagger UI**: `http://localhost:8080/swagger-ui.html` - 전체 API 문서 및 테스트
- **OpenAPI JSON**: `http://localhost:8080/api-docs` - OpenAPI 3.0 스펙 JSON
- H2 Console: `http://localhost:8080/h2-console` (개발환경 전용)

## 📊 데이터베이스 설계

### 🏷️ **QR 테이블 주문 시스템 핵심 테이블**
- **Table**: 테이블 정보 (번호, QR코드, 좌석수, 상태)
- **Order**: 익명 주문 정보 (테이블 기반)
- **Cart**: 익명 장바구니 (테이블별 격리)
- **Payment**: 결제 정보 (간편결제 지원)

### 📋 **메뉴 관리 테이블**
- **Category**: 메뉴 카테고리 (표시순서 관리)
- **Menu**: 메뉴 정보 (카테고리 연관)
- **MenuOption**: 메뉴 옵션 (사이즈, 추가옵션 등)

### 👨‍💼 **관리자 전용 테이블**
- **User**: 관리자 정보 (ADMIN/MANAGER 역할만)
- **OrderItem**: 주문 상세 아이템
- **OrderItemOption**: 주문 아이템별 선택 옵션
- **CartItem**: 장바구니 상세 아이템

## 🔐 인증 방식

**이중 인증 시스템**을 사용합니다.

### 🎯 **소비자 (익명 주문)**
- QR코드 스캔으로 테이블 인식
- **회원가입/로그인 불필요** - 즉시 주문 가능
- 세션 기반 테이블별 장바구니 관리
- 간편결제로 빠른 주문 완료

### 👨‍💼 **관리자 (JWT 인증)**
- JWT(JSON Web Token) 기반 관리자 인증
- 테이블 도식화 대시보드 접근
- 메뉴 관리, 주문 관리, 매출 분석 권한

### 권한 관리
- **ADMIN**: 카페 사장 (모든 권한)
- **MANAGER**: 카페 관리자 (기본 운영 권한)

## 🌟 주요 기능

### 📱 **소비자 QR 주문 시스템**
- ✅ QR코드 스캔으로 테이블 인식
- ✅ 회원가입 없는 익명 주문 (즉시 주문 가능)
- ✅ 테이블별 장바구니 격리 관리
- ✅ 메뉴 옵션 선택 (사이즈, 추가옵션 등)
- ✅ 간편결제 시스템 (카드/모바일 결제)

### 👨‍💼 **관리자 대시보드**
- ✅ 테이블 도식화 메인페이지 (실시간 현황)
- ✅ 일매출/주매출/월매출 통계 조회
- ✅ 테이블 관리 (번호, 도식화 변경)
- ✅ 메뉴판 관리 (메뉴/사진 등록, 변경, 삭제)
- ✅ 주문 처리 (테이블별 서빙 상태 관리)

### 🛠 **시스템 관리 기능**
- ✅ 주문 상태 관리 (대기→준비중→완료→서빙완료)
- ✅ 테이블 상태 관리 (사용가능/사용중/정비중)
- ✅ 인기 메뉴 통계 및 분석
- ✅ 실시간 매출 관리 시스템

## 🧪 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests "com.coffeeplz.*"
```

## 📝 API 명세

### Swagger UI 접속 방법
1. 애플리케이션 실행 후 브라우저에서 `http://localhost:8080/swagger-ui.html` 접속
2. JWT 인증이 필요한 API의 경우:
   - 우상단 "Authorize" 버튼 클릭
   - Bearer Token 형식으로 JWT 토큰 입력: `Bearer {your-jwt-token}`
   - 관리자 로그인 API를 통해 토큰 발급 후 사용

### 주요 API 그룹
- **관리자 인증**: 회원가입, 로그인, 프로필 관리
- **메뉴 관리**: 메뉴 CRUD, 카테고리 관리, 인기 메뉴 조회
- **주문 관리**: 주문 생성, 상태 변경, 통계 조회
- **테이블 관리**: QR 코드 생성, 테이블 상태 관리
- **장바구니**: 테이블별 장바구니 관리
- **결제**: 결제 처리 및 내역 조회

자세한 API 명세는 애플리케이션 실행 후 Swagger UI에서 확인하실 수 있습니다.

## 🚢 CI/CD 파이프라인

### 개요

CoffeePlz는 Jenkins와 Docker를 활용한 완전 자동화된 CI/CD 파이프라인을 제공합니다.

### 파이프라인 구조

```
개발자 코드 푸시 → Jenkins Pipeline → 자동 테스트 → Docker 빌드 → 배포
```

### 환경별 배포

#### 1. 개발 환경 (Development)
```bash
# 개발 환경 실행
docker-compose -f docker-compose.dev.yml up -d

# 또는 배포 스크립트 사용
./scripts/deploy.sh dev latest
```

#### 2. 테스트 환경 (Test)
```bash
# 테스트 환경 실행
docker-compose -f docker-compose.test.yml up --abort-on-container-exit

# 배포 스크립트 사용
./scripts/deploy.sh test latest
```

#### 3. 스테이징 환경 (Staging)
```bash
# 스테이징 환경 배포
./scripts/deploy.sh staging v1.0.0
```

#### 4. 프로덕션 환경 (Production)
```bash
# 프로덕션 환경 배포 (승인 필요)
./scripts/deploy.sh prod v1.0.0
```

### Jenkins 파이프라인 단계

1. **Checkout**: 소스코드 체크아웃
2. **Build**: Gradle을 통한 애플리케이션 빌드
3. **Unit Tests**: 단위 테스트 실행
4. **Code Quality Analysis**: SonarQube를 통한 코드 품질 분석
5. **Security Scan**: 보안 취약점 스캔
6. **Docker Build & Test**: Docker 이미지 빌드 및 컨테이너 테스트
7. **Integration Tests**: 통합 테스트 실행
8. **Push to Registry**: Docker 레지스트리에 이미지 푸시
9. **Deploy to Staging**: 스테이징 환경 자동 배포 (develop 브랜치)
10. **Deploy to Production**: 프로덕션 환경 배포 (main 브랜치, 수동 승인)
11. **Health Check**: 배포 후 헬스체크

### 환경변수 설정

각 환경별로 환경변수 파일을 생성해야 합니다:

```bash
# 환경변수 템플릿 복사
cp config/environment-template.env .env.dev
cp config/environment-template.env .env.test
cp config/environment-template.env .env.staging
cp config/environment-template.env .env.prod

# 각 환경에 맞게 값 수정
vi .env.prod
```

### Docker 명령어

#### 개발 환경
```bash
# 전체 스택 실행
docker-compose -f docker-compose.dev.yml up -d

# 로그 확인
docker-compose -f docker-compose.dev.yml logs -f

# 종료
docker-compose -f docker-compose.dev.yml down
```

#### 프로덕션 환경
```bash
# 환경변수 설정 후 실행
export IMAGE_TAG=v1.0.0
docker-compose -f docker-compose.prod.yml up -d

# 헬스체크
curl http://localhost/actuator/health
```

### 모니터링

#### 애플리케이션 모니터링
- **헬스체크**: `http://localhost:8080/actuator/health`
- **메트릭스**: `http://localhost:8080/actuator/metrics`
- **프로메테우스**: `http://localhost:8080/actuator/prometheus`

#### 로그 모니터링
- 애플리케이션 로그: `./logs/coffeeplz.log`
- Nginx 로그: `./nginx/logs/`
- Docker 로그: `docker-compose logs`

### 백업 및 복구

#### 데이터베이스 백업
```bash
# 수동 백업
docker exec coffeeplz-mysql-prod mysqldump -u root -p coffeeplz > backup.sql

# 백업 복구
docker exec -i coffeeplz-mysql-prod mysql -u root -p coffeeplz < backup.sql
```

### 보안 설정

#### SSL/TLS 인증서
```bash
# Let's Encrypt 인증서 생성 (프로덕션)
certbot certonly --standalone -d your-domain.com

# 인증서 파일을 nginx/ssl/ 디렉토리에 복사
cp /etc/letsencrypt/live/your-domain.com/fullchain.pem nginx/ssl/cert.pem
cp /etc/letsencrypt/live/your-domain.com/privkey.pem nginx/ssl/key.pem
```

### 트러블슈팅

#### 일반적인 문제들

1. **컨테이너 시작 실패**
   ```bash
   # 로그 확인
   docker-compose logs
   
   # 개별 컨테이너 로그 확인
   docker logs coffeeplz-app-prod
   ```

2. **데이터베이스 연결 실패**
   ```bash
   # 데이터베이스 컨테이너 상태 확인
   docker exec coffeeplz-mysql-prod mysql -u root -p -e "SHOW DATABASES;"
   ```

3. **헬스체크 실패**
   ```bash
   # 애플리케이션 상태 확인
   curl -v http://localhost:8080/actuator/health
   ```

### 성능 최적화

#### JVM 튜닝
- 개발환경: `-Xmx512m -Xms256m`
- 프로덕션: `-Xmx1g -Xms512m -XX:+UseG1GC`

#### 데이터베이스 최적화
- 커넥션 풀 크기: 20 (프로덕션)
- 쿼리 캐시 활성화
- 인덱스 최적화

## 🤝 기여 방법

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 라이선스

MIT License 