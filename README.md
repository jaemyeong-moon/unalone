# Unalone - 고독사 방지 커뮤니티 서비스

1인 가구의 안전을 지키는 안부 체크 기반 커뮤니티 서비스입니다.
체크인 데이터를 기반으로 이상 징후를 감지하고, 보호자와 관리자에게 알림을 제공합니다.

---

## 기술 스택

| 분류 | 기술 | 버전 |
|------|------|------|
| Backend | Java, Spring Boot, Spring Security, JPA, Spring Data MongoDB | 17, 3.2.5 |
| Frontend | Next.js, React, TypeScript, Tailwind CSS | 14.2, 18 |
| Database | PostgreSQL, MongoDB | 16, 7 |
| Messaging | Apache Kafka (Confluent) | 7.5.0 |
| Infra | Docker, Docker Compose | latest |
| Monitoring | Prometheus, Grafana | latest |
| Auth | JWT (사용자), HTTP Basic Auth (관리자) | |

---

## 아키텍처

```
┌─────────────┐     ┌──────────────────────────────────────────────┐
│   Browser    │────▶│  Frontend (Next.js :3000)                    │
│              │     │  - SSR + Client Components                   │
│              │     │  - Server-side Rewrites → Backend Services   │
└─────────────┘     └──────────┬────────────────────┬──────────────┘
                               │                    │
                    ┌──────────▼──────┐  ┌──────────▼──────┐
                    │ API Service     │  │ Admin Service    │
                    │ :8080           │  │ :8081            │
                    │ (JWT Auth)      │  │ (Basic Auth)     │
                    └────────┬────────┘  └────────┬────────┘
                             │                    │
                    ┌────────▼────────────────────▼────────┐
                    │          Kafka (Event Bus)            │
                    │  Topics: checkin-events, alert-events │
                    └────────────────┬─────────────────────┘
                                     │
                    ┌────────────────▼─────────────────────┐
                    │        Event Service :8082            │
                    │  - 이벤트 로깅 (MongoDB)              │
                    │  - 미응답 감지 → Alert 생성           │
                    └──────────────────────────────────────┘

         ┌──────────────┐            ┌──────────────┐
         │ PostgreSQL   │            │   MongoDB     │
         │ - User       │            │ - Alert       │
         │ - Profile    │            │ - EventLog    │
         │ - CheckIn    │            └──────────────┘
         │ - Guardian   │
         │ - Community  │
         │ - Product    │
         │ - Order      │
         └──────────────┘
```

---

## 마이크로서비스 구성

### API Service (`:8080`) - 사용자 API

| 메서드 | 엔드포인트 | 설명 | 인증 |
|--------|-----------|------|------|
| POST | `/api/auth/signup` | 회원가입 | - |
| POST | `/api/auth/login` | 로그인 (JWT 발급) | - |
| POST | `/api/checkins` | 안부 체크인 | JWT |
| GET | `/api/checkins` | 체크인 이력 조회 | JWT |
| GET | `/api/checkins/latest` | 최근 체크인 조회 | JWT |
| GET | `/api/profile` | 프로필 조회 | JWT |
| PUT | `/api/profile` | 프로필 수정 | JWT |
| POST | `/api/guardians` | 보호자 등록 (최대 5명) | JWT |
| GET | `/api/guardians` | 보호자 목록 | JWT |
| DELETE | `/api/guardians/{id}` | 보호자 삭제 | JWT |
| POST | `/api/community/posts` | 게시글 작성 | JWT |
| GET | `/api/community/posts` | 게시글 목록 (카테고리 필터) | - |
| GET | `/api/community/posts/{id}` | 게시글 상세 | - |
| DELETE | `/api/community/posts/{id}` | 게시글 삭제 (본인/관리자) | JWT |

### Admin Service (`:8081`) - 관리자 API

> 인증: HTTP Basic Auth (`admin` / `admin`)

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/admin/dashboard` | 대시보드 통계 (사용자수, 체크인, 알림) |
| GET | `/admin/users` | 사용자 목록 (마지막 체크인 시각 포함) |
| PATCH | `/admin/users/{id}/status` | 사용자 상태 변경 |
| GET | `/admin/alerts` | 알림 목록 (상태별 필터) |
| PATCH | `/admin/alerts/{id}/resolve` | 알림 해결 처리 |
| GET | `/admin/products` | 상품 목록 |
| POST | `/admin/products` | 상품 등록 |
| PUT | `/admin/products/{id}` | 상품 수정 |
| DELETE | `/admin/products/{id}` | 상품 삭제 |
| GET | `/admin/orders` | 주문 목록 |
| PATCH | `/admin/orders/{id}/status` | 주문 상태 변경 |
| GET | `/admin/stats/sales` | 매출 통계 |

### Event Service (`:8082`) - 이벤트 처리

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/events` | 이벤트 로그 조회 |
| GET | `/events/type/{type}` | 유형별 이벤트 조회 |
| GET | `/events/health` | 헬스체크 |

**Kafka 이벤트 처리:**
- `checkin-events` → 체크인 이벤트 로깅 + 미응답 시 Alert 생성
- `alert-events` → 알림 이벤트 로깅

**Alert 레벨 기준:**
- 미응답 1회: `WARNING`
- 미응답 2회: `DANGER`
- 미응답 3회 이상: `CRITICAL`

---

## 프론트엔드 페이지

| 경로 | 페이지 | 설명 |
|------|--------|------|
| `/` | 홈 | 비로그인: 랜딩 / 로그인: 대시보드+체크인 |
| `/login` | 로그인 | 이메일/비밀번호 로그인 |
| `/signup` | 회원가입 | 이름, 이메일, 비밀번호, 전화번호 |
| `/community` | 커뮤니티 | 게시판 (카테고리: 일상/건강/취미/도움요청/공지) |
| `/community/[id]` | 게시글 상세 | 게시글 내용 + 삭제 |
| `/profile` | 프로필 | 체크인 간격, 활동시간, 주소, 비상메모 |
| `/guardians` | 보호자 관리 | 보호자 등록/삭제 (최대 5명) |
| `/admin` | 관리자 대시보드 | 통계 카드, 알림 현황 |
| `/admin/users` | 사용자 관리 | 사용자 목록, 상태 변경 |
| `/admin/alerts` | 알림 관리 | 알림 목록, 해결 처리 |

---

## 실행 방법

### 사전 요구사항

- Docker Desktop
- Git

### 전체 서비스 실행

```bash
# 프로젝트 클론
git clone <repository-url>
cd ai-project-unalone

# 전체 서비스 실행 (인프라 + 백엔드 + 프론트엔드 + 모니터링)
docker compose up -d

# 서비스 상태 확인
docker compose ps

# 로그 확인
docker compose logs -f
```

### 단계별 실행 (개발용)

```bash
# 1. 인프라만 실행
docker compose up -d zookeeper kafka postgresql mongodb

# 2. 백엔드 서비스 실행
docker compose up -d api-service admin-service event-service

# 3. 프론트엔드 실행
docker compose up -d frontend

# 4. 모니터링 실행
docker compose up -d kafka-ui prometheus grafana
```

### 로컬 개발 (Docker 인프라 + 로컬 서비스)

```bash
# 인프라만 Docker로 실행
docker compose up -d zookeeper kafka postgresql mongodb

# 백엔드 로컬 실행
cd backend && ./gradlew :api-service:bootRun
cd backend && ./gradlew :admin-service:bootRun
cd backend && ./gradlew :event-service:bootRun

# 프론트엔드 로컬 실행
cd frontend && npm install && npm run dev
```

### 서비스 중지

```bash
# 전체 중지
docker compose down

# 데이터 포함 전체 삭제
docker compose down -v
```

---

## 서비스 접속 URL

| 서비스 | URL | 비고 |
|--------|-----|------|
| 프론트엔드 | http://localhost:3000 | 메인 서비스 |
| API Service | http://localhost:8080 | Swagger: `/swagger-ui.html` |
| Admin Service | http://localhost:8081 | Swagger: `/swagger-ui.html` |
| Event Service | http://localhost:8082 | |
| Kafka UI | http://localhost:9093 | Kafka 토픽 모니터링 |
| Prometheus | http://localhost:9090 | 메트릭 조회 |
| Grafana | http://localhost:3001 | admin / admin |

---

## Kafka 이벤트 흐름

```
[API Service]                    [Event Service]              [Admin Service]
     │                                │                            │
     ├── CheckInCompletedEvent ──────▶│ 로깅 + Alert 판단          │
     ├── CheckInMissedEvent ─────────▶│ 미응답 횟수별 Alert 생성    │
     │                                │                            │
     │                                ├── AlertCreatedEvent ──────▶│ 대시보드 반영
     │                                │                            │
     │◀── AlertResolvedEvent ────────────────────────────────────── │ Alert 해결
     │                                                             │
     │                                              StockUpdatedEvent ──┤ 재고 변동
     │                                              OrderStatusChanged ─┤ 주문 상태
```

---

## 데이터베이스 스키마

### PostgreSQL

| 테이블 | 설명 | 주요 컬럼 |
|--------|------|----------|
| `users` | 사용자 | email, password(BCrypt), name, phone, role, status |
| `profiles` | 프로필 (User 1:1) | checkIntervalHours, activeHoursStart/End, address, emergencyNote |
| `check_ins` | 체크인 (User N:1) | status(CHECKED/MISSED), message, checkedAt |
| `guardians` | 보호자 (User N:1, 최대 5명) | name, phone, relationship |
| `community_posts` | 커뮤니티 게시글 (User N:1) | title, content, category |
| `products` | 상품 (낙관적 잠금) | name, price, stock, version |
| `orders` / `order_items` | 주문 | status, totalAmount, items |

### MongoDB

| 컬렉션 | 설명 | 주요 필드 |
|--------|------|----------|
| `alerts` | 알림 | userId, level(WARNING/DANGER/CRITICAL), status, resolvedBy |
| `event_logs` | 이벤트 로그 | eventType, payload, status, occurredAt, processedAt |

---

## 프로젝트 구조

```
ai-project-unalone/
├── backend/
│   ├── common/                  # 공통 모듈 (DTO, Event, Kafka Config)
│   │   └── src/main/java/com/project/common/
│   │       ├── config/          # KafkaConfig
│   │       ├── dto/             # ApiResponse, PageResponse
│   │       └── event/           # DomainEvent, EventPublisher, 이벤트 클래스들
│   ├── api-service/             # 사용자 API 서비스
│   │   └── src/main/java/com/project/api/
│   │       ├── config/          # SecurityConfig (JWT)
│   │       ├── controller/      # Auth, CheckIn, Community, Guardian, Profile
│   │       ├── domain/          # User, Profile, CheckIn, Guardian, CommunityPost
│   │       ├── dto/             # Request/Response DTOs
│   │       ├── exception/       # BusinessException, GlobalExceptionHandler
│   │       ├── kafka/           # Kafka Producer/Consumer
│   │       ├── repository/      # JPA Repositories
│   │       ├── security/        # JwtTokenProvider, JwtAuthenticationFilter
│   │       └── service/         # Business Logic + CheckInScheduler
│   ├── admin-service/           # 관리자 API 서비스
│   │   └── src/main/java/com/project/admin/
│   │       ├── config/          # SecurityConfig (Basic Auth)
│   │       ├── controller/      # Dashboard, User, Alert, Product, Order, Stats
│   │       ├── domain/          # User, Alert, Product, Order (JPA + MongoDB)
│   │       ├── dto/             # Response/Request DTOs
│   │       ├── kafka/           # Kafka Producer/Consumer
│   │       ├── repository/      # JPA + Mongo Repositories
│   │       └── service/         # Admin Business Logic
│   └── event-service/           # 이벤트 처리 서비스
│       └── src/main/java/com/project/event/
│           ├── controller/      # EventLogController
│           ├── domain/          # Alert, EventLog
│           ├── handler/         # AlertHandler, EventHandler
│           ├── kafka/           # EventConsumer
│           └── repository/      # MongoDB Repositories
├── frontend/
│   └── src/
│       ├── app/                 # Next.js App Router 페이지
│       │   ├── admin/           # 관리자 (대시보드, 사용자, 알림)
│       │   ├── community/       # 커뮤니티 (목록, 상세)
│       │   ├── guardians/       # 보호자 관리
│       │   ├── login/           # 로그인
│       │   ├── profile/         # 프로필
│       │   └── signup/          # 회원가입
│       ├── components/common/   # 공통 컴포넌트 (Header)
│       ├── lib/                 # API 클라이언트, 인증 유틸리티
│       └── types/               # TypeScript 타입 정의
├── infra/
│   ├── prometheus/              # Prometheus 설정
│   └── grafana/                 # Grafana 데이터소스 설정
└── docker-compose.yml           # 전체 서비스 오케스트레이션 (11개 서비스)
```
