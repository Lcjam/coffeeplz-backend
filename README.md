# CoffeePlz ☕

온라인 카페 주문 시스템 백엔드 API

## 📋 프로젝트 소개

Spring Boot 기반의 온라인 카페 주문 시스템입니다. 메뉴 관리, 주문 처리, 포인트 결제, 리뷰 시스템 등의 기능을 제공합니다.

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
│   │   ├── service/         # 비즈니스 로직
│   │   ├── repository/      # 데이터 접근 계층
│   │   ├── entity/          # JPA 엔티티
│   │   ├── dto/             # 데이터 전송 객체
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

### 주요 테이블
- **User**: 사용자 정보
- **Menu**: 메뉴 정보  
- **Order**: 주문 정보
- **OrderItem**: 주문 상세
- **Cart**: 장바구니
- **CartItem**: 장바구니 상세
- **Review**: 리뷰
- **PointHistory**: 포인트 이력

## 🔐 인증 방식

JWT(JSON Web Token) 기반 인증을 사용합니다.

### 권한 관리
- **CUSTOMER**: 일반 고객
- **ADMIN**: 관리자

## 🌟 주요 기능

- ✅ 회원가입/로그인 (JWT 인증)
- ✅ 메뉴 CRUD (관리자)
- ✅ 장바구니 관리
- ✅ 주문 생성/조회
- ✅ 포인트 충전/결제
- ✅ 리뷰 시스템
- ✅ 인기 메뉴 조회

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