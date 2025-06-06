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
- Swagger UI: `http://localhost:8080/swagger-ui.html`
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