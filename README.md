# CoffeePlz ☕

QR코드 기반 카페 테이블 주문 시스템 백엔드 API

## 📋 프로젝트 소개

Spring Boot 기반의 **QR코드 테이블 주문 시스템**입니다. 카페 테이블에 QR코드를 배치하여 고객이 직접 스캔하고 주문할 수 있는 현대적인 카페 운영 솔루션을 제공합니다.

### 🎯 **핵심 기능**
- **📱 QR코드 스캔**: 테이블별 고유 QR코드로 즉시 메뉴 접근
- **🍽️ 테이블 주문**: 비회원도 가능한 간편한 테이블 주문
- **👤 회원 혜택**: 포인트 적립/사용, 리뷰 작성 등 부가 서비스  
- **⚡ 실시간 관리**: 테이블별 주문 현황 실시간 모니터링

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
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Console: `http://localhost:8080/h2-console` (개발환경 전용)

## 📊 데이터베이스 설계

### 🏷️ **QR 테이블 주문 시스템 테이블**
- **Table**: 테이블 정보 (번호, QR코드, 좌석수, 상태)
- **Order**: 주문 정보 (테이블/회원 기반)
- **Cart**: 장바구니 (테이블별 격리)

### 📋 **기본 시스템 테이블**
- **User**: 회원 정보 (선택적 회원 가입)
- **Menu**: 메뉴 정보  
- **OrderItem**: 주문 상세
- **CartItem**: 장바구니 상세
- **Review**: 리뷰 (회원 전용)
- **PointHistory**: 포인트 이력 (회원 전용)

## 🔐 인증 방식

**하이브리드 인증 시스템**을 사용합니다.

### 🎯 **QR 테이블 주문** (비회원)
- QR코드 스캔으로 테이블 인식
- 별도 로그인 없이 즉시 주문 가능
- 세션 기반 테이블별 장바구니 관리

### 👤 **회원 주문** (JWT 인증)
- JWT(JSON Web Token) 기반 인증
- 포인트 적립/사용, 주문 이력, 리뷰 작성 가능

### 권한 관리
- **CUSTOMER**: 일반 회원 고객
- **ADMIN**: 카페 사장/관리자

## 🌟 주요 기능

### 📱 **QR 테이블 주문 시스템**
- ✅ QR코드 스캔 테이블 인식
- ✅ 비회원 테이블 주문 (즉시 주문)
- ✅ 테이블별 장바구니 격리 관리
- ✅ 테이블 상태 실시간 관리 (사용가능/사용중/정비중)
- ✅ 관리자용 테이블별 주문 현황 대시보드

### 👤 **회원 서비스** 
- ✅ 회원가입/로그인 (JWT 인증)
- ✅ 포인트 적립/사용 시스템
- ✅ 주문 이력 관리
- ✅ 리뷰 작성/조회

### 🛠 **공통 기능**
- ✅ 메뉴 CRUD (관리자)
- ✅ 주문 상태 관리 (대기→준비→완료)
- ✅ 인기 메뉴 통계
- ✅ 매출 관리 시스템

## 🧪 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests "com.coffeeplz.*"
```

## 📝 API 명세

자세한 API 명세는 애플리케이션 실행 후 Swagger UI에서 확인하실 수 있습니다.

## 🚢 배포

### Jenkins CI/CD 파이프라인
1. GitHub Push → Webhook 트리거
2. 빌드 및 테스트 실행
3. Docker 이미지 생성
4. Docker Compose 배포

## 🤝 기여 방법

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 라이선스

MIT License 