# Unalone - 고독사 방지 커뮤니티 서비스

## 프로젝트 개요
1인 가구 안전을 위한 안부 체크 기반 커뮤니티 서비스. 체크인 데이터 기반 이상 징후 감지 및 보호자/관리자 알림 제공.

## 아키텍처
- **마이크로서비스**: API Service(:8080), Admin Service(:8081), Event Service(:8082)
- **프론트엔드**: Next.js(:3000) - App Router, SSR + Client Components
- **메시징**: Apache Kafka (checkin-events, alert-events 토픽)
- **DB**: PostgreSQL(사용자/체크인/커뮤니티), MongoDB(알림/이벤트로그)
- **인프라**: Docker Compose, Prometheus, Grafana

## 기술 스택
- Backend: Java 17, Spring Boot 3.2.5, Spring Security, JPA, Spring Data MongoDB
- Frontend: Next.js 14.2, React 18, TypeScript, Tailwind CSS
- DB: PostgreSQL 16, MongoDB 7
- Messaging: Kafka (Confluent 7.5.0)

## 주요 디렉토리 구조
```
backend/
  common/          # 공통 모듈 (DTO, Event, Kafka Config)
  api-service/     # 사용자 API (JWT 인증)
  admin-service/   # 관리자 API (Basic Auth)
  event-service/   # Kafka 이벤트 처리 + MongoDB 로깅
frontend/
  src/app/         # Next.js App Router 페이지
  src/components/  # 공통 컴포넌트
  src/lib/         # API 클라이언트, 인증 유틸
  src/types/       # TypeScript 타입 정의
infra/             # Prometheus, Grafana 설정
k8s/               # Kubernetes 매니페스트
```

## 공통 규칙
- 모든 코드 및 커밋 메시지는 한국어 주석/설명 허용, 코드는 영어
- API 응답은 `ApiResponse<T>` 래퍼 사용 (common 모듈)
- 이벤트 기반 통신: Kafka를 통한 서비스 간 비동기 메시징
- 에러 처리: `BusinessException` + `GlobalExceptionHandler` 패턴

## 빌드 & 실행
```bash
# 전체 실행
docker compose up -d

# 인프라만
docker compose up -d zookeeper kafka postgresql mongodb

# 백엔드 로컬
cd backend && ./gradlew :api-service:bootRun
cd backend && ./gradlew :admin-service:bootRun
cd backend && ./gradlew :event-service:bootRun

# 프론트엔드 로컬
cd frontend && npm install && npm run dev
```

## Agent Team 역할 분담
각 에이전트는 담당 영역의 `CLAUDE.md`를 참조하여 작업합니다:

### 개발팀
- **Backend Agent**: `backend/CLAUDE.md` 참조 - Java/Spring Boot 마이크로서비스 개발
- **Frontend Agent**: `frontend/CLAUDE.md` 참조 - Next.js UI 개발
- **Infra Agent**: `infra/CLAUDE.md` 참조 - Docker, K8s, 클라우드 환경별 배포 설정

### 비개발팀
- **Planning Agent (기획팀)**: `docs/planning/CLAUDE.md` 참조 - 기능 명세, 사용자 스토리, 우선순위 결정
- **Design Agent (디자인팀)**: `docs/design/CLAUDE.md` 참조 - UI/UX 설계, 컴포넌트/화면 설계서
- **QA Agent (테스터팀)**: `docs/qa/CLAUDE.md` 참조 - 테스트 계획, 버그 리포트, 품질 관리

### 협업 규칙
- 서비스 간 인터페이스 변경 시 반드시 관련 에이전트에게 알림 필요
- 기획 → 디자인 → 개발 → QA 순서로 산출물 전달
- 각 팀 산출물은 해당 `docs/` 하위 디렉토리에 저장
