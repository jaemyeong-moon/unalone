# 프로젝트 아이디어 입력 템플릿 — Unalone

---

## 1. 프로젝트 이름

Unalone (고독사 방지 커뮤니티 서비스)

---

## 2. 프로젝트 설명 (한 줄 요약)

혼자 사는 사용자의 안부 체크 데이터를 기반으로 이상 징후를 감지하고 보호자 및 관리자에게 알림을 제공하는 안전 네트워크 서비스

---

## 3. 핵심 기능 목록 (MVP)

- [ ] OAuth 로그인 (카카오 / 구글)
- [ ] 사용자 프로필 및 체크 주기 설정
- [ ] 일일 안부 체크
- [ ] 미응답 감지 및 자동 알림
- [ ] 보호자 등록
- [ ] 커뮤니티 게시판
- [ ] 관리자 모니터링 대시보드
- [ ] 권한 기반 접근 제어 (USER / ADMIN)

---

## 4. 도메인 모델 정의

| 엔티티 | 주요 필드 | 저장소 | 설명 |
|--------|----------|--------|------|
| User | id, oauthProvider, oauthId, name, role, status | PostgreSQL | 사용자 계정 |
| Profile | userId, checkInterval, activeHours | PostgreSQL | 생활 패턴 |
| Guardian | id, userId, name, phone | PostgreSQL | 보호자 |
| CheckIn | id, userId, status, checkedAt | PostgreSQL | 안부 기록 |
| Alert | id, userId, level, message, createdAt, resolvedAt | MongoDB | 이상 알림 |
| CommunityPost | id, userId, title, content, createdAt | MongoDB | 게시글 |
| EventLog | id, eventType, payload, timestamp | MongoDB | 이벤트 로그 |

### 관계

User (1) ──── (1) Profile  
User (1) ──── (N) Guardian  
User (1) ──── (N) CheckIn  
User (1) ──── (N) Alert  

---

## 5. API 엔드포인트

### API Service (USER)

| METHOD | PATH | DESC | AUTH |
|---|---|---|---|
| GET | /api/auth/oauth/{provider} | OAuth 로그인 | X |
| GET | /api/profile | 프로필 조회 | USER |
| PUT | /api/profile | 프로필 수정 | USER |
| POST | /api/checkins | 안부 체크 | USER |
| GET | /api/alerts | 내 알림 | USER |
| POST | /api/guardians | 보호자 등록 | USER |
| GET | /api/community/posts | 게시글 목록 | USER |
| POST | /api/community/posts | 게시글 작성 | USER |

### Admin Service

| METHOD | PATH | DESC | AUTH |
|---|---|---|---|
| GET | /admin/users | 사용자 목록 | ADMIN |
| GET | /admin/alerts | 알림 목록 | ADMIN |
| PATCH | /admin/alerts/{id}/resolve | 알림 종료 | ADMIN |
| GET | /admin/dashboard | 위험 사용자 | ADMIN |
| DELETE | /admin/community/posts/{id} | 게시글 삭제 | ADMIN |

---

## 6. 이벤트 정의

| EVENT | TOPIC | PRODUCER | CONSUMER | PAYLOAD |
|---|---|---|---|---|
| CheckInCompleted | checkin-events | API | Event | { userId, checkedAt } |
| CheckInMissed | alert-events | Event | API, Admin | { userId, expectedAt } |
| AlertCreated | alert-events | Event | Admin | { alertId, userId, level } |
| AlertResolved | alert-events | Admin | Event | { alertId, resolvedAt } |

---

## 7. 관리자 기능

- 사용자 상태 모니터링
- 미응답 사용자 확인
- 알림 종료 처리
- 게시글 관리
- 통계 대시보드

---

## 8. 비즈니스 규칙

1. 체크 주기 내 안부 체크 없으면 Alert 생성  
2. Alert 단계는 주의 → 위험 → 긴급 순으로 상승  
3. 긴급 단계 시 보호자 알림 전송  
4. 관리자만 Alert 종료 가능  
5. USER는 관리자 API 접근 불가  

---   `

## 9. 비기능 요구사항

### 성능
- P95 500ms 이하
- 동시 사용자 5,000

### 보안
- OAuth2 기반 인증
- RBAC 권한 관리
- 개인정보 암호화

### 가용성
- 99.9% uptime
- 이벤트 at-least-once

### 관측성
- Prometheus / Grafana
- 분산 로그
- Kafka 이벤트 스트림

---
## 10. 마이크로서비스 분리 (Bounded Context)

### 1️⃣ Auth Service
역할: OAuth 로그인, 토큰 발급, 사용자 권한 관리

책임
- OAuth 인증 (카카오, 구글)
- JWT 발급/검증
- USER / ADMIN Role 관리

DB
- User

---

### 2️⃣ Checkin Service
역할: 안부 체크 기록 및 패턴 관리

책임
- 체크 기록 저장
- 체크 주기 계산
- 미응답 이벤트 발행

DB
- CheckIn
- Profile

---

### 3️⃣ Alert Service
역할: 이상 감지 및 알림 관리

책임
- 미응답 감지
- Alert 생성
- 보호자 알림 트리거

DB
- Alert (MongoDB)

---

### 4️⃣ Community Service
역할: 커뮤니티 기능

책임
- 게시글 CRUD
- 신고 및 관리자 삭제

DB
- CommunityPost (MongoDB)

---

### 5️⃣ Admin Service
역할: 운영 및 모니터링

책임
- 사용자 상태 조회
- Alert 관리
- 통계 제공

---

## 11. Spring Boot 멀티모듈 구조

unalone
 ┣ common
 ┃ ┣ security
 ┃ ┣ dto
 ┃ ┗ util
 ┣ auth-service
 ┣ checkin-service
 ┣ alert-service
 ┣ community-service
 ┣ admin-service
 ┗ gateway

---

## 12. Kafka 토픽 설계

| Topic | 설명 |
|---|---|
| checkin.completed | 안부 체크 완료 |
| checkin.missed | 체크 미응답 |
| alert.created | Alert 생성 |
| alert.resolved | Alert 종료 |
| notification.requested | 알림 요청 |

---

## 13. 이벤트 플로우

1️⃣ 사용자가 체크 수행  
→ checkin.completed 발행  

2️⃣ 스케줄러가 미응답 감지  
→ checkin.missed 발행  

3️⃣ Alert Service  
→ alert.created 발행  

4️⃣ Admin 확인 후 종료  
→ alert.resolved 발행  

---

## 14. PostgreSQL DDL

### User

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    oauth_provider VARCHAR(20) NOT NULL,
    oauth_id VARCHAR(100) NOT NULL,
    name VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Profile

```sql
CREATE TABLE profiles (
    user_id BIGINT PRIMARY KEY,
    check_interval INT NOT NULL,
    active_hours VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Guardian

```sql
CREATE TABLE guardians (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    name VARCHAR(50),
    phone VARCHAR(20),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### CheckIn

```sql
CREATE TABLE checkins (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    status VARCHAR(20),
    checked_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 15. MongoDB Collection Schema

### Alert

```json
{
  "_id": "ObjectId",
  "userId": 1,
  "level": "WARNING",
  "message": "Check-in missed",
  "createdAt": "ISODate",
  "resolvedAt": null
}
```

### CommunityPost

```json
{
  "_id": "ObjectId",
  "userId": 1,
  "title": "오늘 날씨 좋네요",
  "content": "...",
  "createdAt": "ISODate"
}
```

---

## 16. 권한 모델 (RBAC)

ROLE_USER
- 본인 데이터 접근
- 체크 수행
- 커뮤니티 이용

ROLE_ADMIN
- 전체 사용자 조회
- Alert 관리
- 게시글 삭제
- 통계 조회

---

## 17. 배포 아키텍처 (권장)

Client → API Gateway → Services  

구성
- Gateway (인증 필터)
- 각 서비스 독립 배포
- Kafka Cluster
- PostgreSQL (RDS)
- MongoDB (Replica Set)
- Redis (토큰 캐시)

---

## 18. 향후 확장 로드맵

Phase 2
- AI 위험 점수 예측
- IoT 연동 (스마트 플러그 / 활동 감지)

Phase 3
- 지자체 연동 API
- 긴급 출동 파트너 연계

---