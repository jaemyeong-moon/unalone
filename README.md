# Event-Driven Microservices Template

이벤트 기반 마이크로서비스 아키텍처 템플릿 프로젝트입니다. Spring Boot 백엔드 3개 서비스, Next.js 프론트엔드, Kafka 메시지 브로커를 중심으로 구성되어 있습니다.

---

## 아키텍처 개요

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Frontend    │────▶│ API Service │────▶│  PostgreSQL  │
│  (Next.js)   │     │ (Port 8080) │     │ (Port 5432)  │
└─────────────┘     └──────┬──────┘     └──────────────┘
                           │
                    ┌──────▼──────┐
                    │    Kafka     │
                    │ (Port 9092)  │
                    └──┬───────┬──┘
                       │       │
              ┌────────▼──┐ ┌──▼──────────┐
              │  Admin     │ │   Event      │
              │  Service   │ │   Service    │     ┌──────────┐
              │ (Port 8081)│ │ (Port 8082)  │────▶│ MongoDB  │
              └────────────┘ └─────────────┘     │(Port 27017)│
                                                  └──────────┘
```

- **API Service**: 클라이언트 요청을 처리하는 메인 API 서비스
- **Admin Service**: 관리자 전용 기능을 담당하는 서비스
- **Event Service**: 이벤트를 소비하고 비동기 작업을 처리하는 서비스
- **Frontend**: 사용자 인터페이스를 제공하는 Next.js 애플리케이션

---

## 기술 스택

| 구분 | 기술 | 버전 |
|------|------|------|
| 백엔드 프레임워크 | Spring Boot | 3.x |
| 프론트엔드 | Next.js | 14.x |
| 메시지 브로커 | Apache Kafka | 7.5.0 (Confluent) |
| RDB | PostgreSQL | 16 |
| NoSQL | MongoDB | 7 |
| 컨테이너 | Docker + Docker Compose | latest |
| 오케스트레이션 | Kubernetes | 1.28+ |
| 모니터링 | Prometheus + Grafana | latest |

---

## 빠른 시작

### 사전 요구사항

- Docker 및 Docker Compose 설치
- Java 17+ (로컬 개발 시)
- Node.js 18+ (로컬 개발 시)

### 전체 서비스 실행

```bash
# 모든 서비스를 한 번에 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 서비스 중지
docker-compose down
```

### 인프라만 실행 (로컬 개발 시)

```bash
# 인프라 서비스만 실행 (DB, Kafka 등)
docker-compose -f docker-compose.infra.yml up -d
```

---

## 서비스 목록

| 서비스 | 포트 | 설명 |
|--------|------|------|
| API Service | `8080` | 클라이언트 대상 REST API |
| Admin Service | `8081` | 관리자 대시보드 및 관리 API |
| Event Service | `8082` | Kafka 이벤트 소비 및 비동기 처리 |
| Frontend | `3000` | Next.js 웹 애플리케이션 |
| Kafka UI | `9093` | Kafka 토픽/메시지 디버깅 도구 |
| Prometheus | `9090` | 메트릭 수집 및 조회 |
| Grafana | `3001` | 메트릭 시각화 대시보드 |

---

## 개발 가이드

### 각 서비스 개별 실행 방법

인프라를 먼저 실행한 뒤, 각 서비스를 로컬에서 개별적으로 실행합니다.

```bash
# 1. 인프라 실행
docker-compose -f docker-compose.infra.yml up -d
```

**API Service**

```bash
cd backend/api-service
./gradlew bootRun
# http://localhost:8080 에서 접근
```

**Admin Service**

```bash
cd backend/admin-service
./gradlew bootRun
# http://localhost:8081 에서 접근
```

**Event Service**

```bash
cd backend/event-service
./gradlew bootRun
# http://localhost:8082 에서 접근
```

**Frontend**

```bash
cd frontend
npm install
npm run dev
# http://localhost:3000 에서 접근
```

---

## 프로젝트 구조

```
ai-project/
├── backend/
│   ├── api-service/          # 메인 API 서비스 (Spring Boot)
│   │   ├── src/
│   │   ├── build.gradle
│   │   └── Dockerfile
│   ├── admin-service/        # 관리자 서비스 (Spring Boot)
│   │   ├── src/
│   │   ├── build.gradle
│   │   └── Dockerfile
│   └── event-service/        # 이벤트 처리 서비스 (Spring Boot)
│       ├── src/
│       ├── build.gradle
│       └── Dockerfile
├── frontend/                 # 프론트엔드 (Next.js)
│   ├── src/
│   ├── package.json
│   └── Dockerfile
├── infra/
│   ├── k8s/                  # Kubernetes 매니페스트
│   ├── prometheus/           # Prometheus 설정
│   └── grafana/              # Grafana 대시보드 설정
├── docker-compose.yml        # 전체 서비스 실행
├── docker-compose.infra.yml  # 인프라만 실행
├── IDEA_TEMPLATE.md          # 프로젝트 아이디어 입력 템플릿
├── .gitignore
└── README.md
```

---

## 모니터링

서비스 실행 후 아래 주소에서 모니터링 도구에 접근할 수 있습니다.

- **Prometheus**: [http://localhost:9090](http://localhost:9090) - 메트릭 수집 및 쿼리
- **Grafana**: [http://localhost:3001](http://localhost:3001) - 대시보드 시각화 (기본 계정: admin / admin)
- **Kafka UI**: [http://localhost:9093](http://localhost:9093) - Kafka 토픽 및 메시지 모니터링
