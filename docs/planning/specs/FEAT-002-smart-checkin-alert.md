# FEAT-002: 스마트 체크인 알림 시스템 (Smart Check-in Reminder & Escalation)

## 목적
현재 체크인 미응답 시 단순 단계별 알림(WARNING/DANGER/CRITICAL)만 제공하고 있다. 스마트 체크인 알림 시스템을 도입하여 다음 목표를 달성한다:

1. **사전 예방 강화**: 체크인 시간 도래 전 리마인더를 발송하여 미응답률 감소
2. **단계별 에스컬레이션**: SMS → 보호자 알림 → 관리자 알림 → 긴급 연락(119) 순서로 자동 에스컬레이션
3. **개인화 스케줄**: 사용자별 생활 패턴에 맞는 체크인 스케줄 설정 지원
4. **AI 이상 감지**: 체크인 패턴 분석을 통한 이상 징후 조기 발견 (시간대 변화, 빈도 감소 등)

## 대상 사용자
- **일반 사용자 (ROLE_USER)**: 체크인 리마인더 수신 및 개인 스케줄 설정
- **보호자**: 에스컬레이션 단계에 따른 알림 수신
- **관리자 (ROLE_ADMIN)**: 에스컬레이션 현황 모니터링, 긴급 대응 관리

## 선행 조건
- 체크인 시스템 구현 완료 (현재 완료 상태)
- 보호자 관리 기능 구현 완료 (현재 완료 상태)
- Kafka 이벤트 시스템 작동 중 (checkin-events, alert-events 토픽)
- SMS 발송 외부 서비스 연동 준비 (NHN Cloud Toast SMS 또는 AWS SNS)

---

## 사용자 스토리

### US-001: 체크인 리마인더 수신
```
AS A 1인 가구 사용자
I WANT TO 체크인 시간에 알림을 받고 싶다
SO THAT 체크인을 잊지 않고 수행하여 안전을 확인받을 수 있다
```

### US-002: 개인 체크인 스케줄 설정
```
AS A 1인 가구 사용자
I WANT TO 나의 생활 패턴에 맞는 체크인 스케줄을 설정하고 싶다
SO THAT 불필요한 알림 없이 적절한 시간에 체크인할 수 있다
```

### US-003: 보호자 에스컬레이션 알림
```
AS A 보호자
I WANT TO 내가 돌보는 사용자가 체크인에 반복적으로 미응답할 때 즉시 알림을 받고 싶다
SO THAT 신속하게 안부를 확인하고 위험 상황에 대응할 수 있다
```

### US-004: 관리자 에스컬레이션 대시보드
```
AS A 관리자
I WANT TO 현재 에스컬레이션 진행 중인 사용자 목록을 실시간으로 확인하고 싶다
SO THAT 긴급 상황에 즉시 대응할 수 있다
```

### US-005: AI 이상 패턴 감지 알림
```
AS A 관리자
I WANT TO 평소와 다른 체크인 패턴을 보이는 사용자를 자동으로 감지하고 싶다
SO THAT 위험 징후를 사전에 포착하여 예방적 조치를 취할 수 있다
```

---

## 기능 상세

### 1. 주요 흐름 (Happy Path)

#### 1-1. 체크인 리마인더 발송
```
1. 스케줄러가 매 분마다 체크인 예정 사용자 목록 조회
2. 체크인 예정 시간 30분 전 사용자에게 푸시/앱 내 알림 발송
3. 체크인 예정 시간 도래 시 최종 리마인더 발송
4. 사용자가 체크인 수행 → 리마인더 종료
```

#### 1-2. 미응답 에스컬레이션 흐름
```
1. 체크인 예정 시간 초과 (1단계 - REMINDER)
   → 사용자에게 앱 내 알림 + SMS 발송
   → escalation-events Kafka 토픽에 이벤트 발행

2. 체크인 예정 시간 + 1시간 초과 (2단계 - WARNING)
   → 사용자에게 재차 SMS 발송
   → 등록된 보호자 전원에게 알림 발송 (앱 내 + SMS)
   → alert-events Kafka 토픽에 WARNING 이벤트 발행

3. 체크인 예정 시간 + 3시간 초과 (3단계 - DANGER)
   → 관리자 대시보드에 위험 알림 표시
   → 관리자에게 알림 발송
   → 보호자에게 재차 알림 ("연락이 닿지 않고 있습니다")
   → alert-events Kafka 토픽에 DANGER 이벤트 발행

4. 체크인 예정 시간 + 6시간 초과 (4단계 - CRITICAL)
   → 관리자에게 긴급 알림 발송
   → 관리자 대시보드에 CRITICAL 표시, 긴급 연락처(119) 안내
   → alert-events Kafka 토픽에 CRITICAL 이벤트 발행
   → 해당 사용자 주소 정보를 관리자에게 표시

5. 사용자가 체크인을 수행하면 어떤 단계에서든 에스컬레이션 즉시 종료
   → escalation-resolved 이벤트 발행
```

#### 1-3. 개인 체크인 스케줄 설정
```
1. 사용자가 프로필 설정 페이지에서 체크인 스케줄 설정
2. 체크인 간격 선택: 12시간 / 24시간 / 48시간
3. 선호 체크인 시간대 설정: 시작 시간, 종료 시간 (예: 09:00~11:00)
4. 요일별 활성/비활성 설정 (예: 주말 제외)
5. 임시 비활성 기간 설정 (여행, 입원 등)
6. 설정 저장 시 다음 체크인 예정 시간 자동 계산
```

#### 1-4. AI 이상 패턴 감지
```
1. 배치 작업이 매일 02:00에 전체 사용자 체크인 이력 분석
2. 최근 30일 데이터 기반으로 사용자별 체크인 패턴 프로파일 생성
3. 다음 이상 징후 감지:
   a. 체크인 시간대 급격한 변화 (평소 09:00 → 최근 15:00)
   b. 체크인 빈도 감소 (주 7회 → 주 3회)
   c. 체크인 메모/상태 변화 (감정 점수 급락 - FEAT-004 연동)
   d. 에스컬레이션 빈도 증가 (월 0회 → 월 3회)
4. 이상 징후 점수가 임계값 초과 시 관리자에게 알림
5. anomaly-events Kafka 토픽에 이벤트 발행
```

### 2. 예외 흐름 (Edge Cases)

#### 2-1. SMS 발송 실패
```
- 조건: 외부 SMS 서비스 장애 또는 잘못된 전화번호
- 처리: 3회 재시도 (5초, 30초, 2분 간격), 실패 시 앱 내 알림으로 대체
- 로그: 실패 이력 MongoDB에 기록, 관리자 대시보드에 표시
```

#### 2-2. 보호자 미등록 사용자의 에스컬레이션
```
- 조건: 보호자가 등록되지 않은 사용자가 2단계(WARNING) 도달
- 처리: 보호자 알림 단계를 건너뛰고 즉시 관리자 알림으로 에스컬레이션
- 추가: 관리자 알림에 "보호자 미등록 사용자" 표시
```

#### 2-3. 임시 비활성 기간 중 체크인
```
- 조건: 임시 비활성 기간으로 설정했으나 체크인을 수행
- 처리: 정상 체크인으로 기록, 비활성 기간 중에는 리마인더/에스컬레이션 미발송
```

#### 2-4. 사용자 탈퇴/계정 비활성 중 에스컬레이션
```
- 조건: 에스컬레이션 진행 중 사용자 계정이 비활성화
- 처리: 진행 중인 에스컬레이션 즉시 종료, 관리자에게 알림
```

#### 2-5. 동시 다수 사용자 에스컬레이션
```
- 조건: 시스템 장애 등으로 다수 사용자가 동시에 에스컬레이션 진입
- 처리: SMS 발송을 큐 기반으로 처리, 관리자 대시보드에 일괄 표시
- 한도: SMS 발송은 분당 100건 제한 (rate limiting)
```

### 3. 비즈니스 규칙

| 규칙 ID | 규칙 | 설명 |
|---------|------|------|
| BR-001 | 에스컬레이션 4단계 구조 | REMINDER(0h) → WARNING(+1h) → DANGER(+3h) → CRITICAL(+6h) |
| BR-002 | 체크인 수행 시 즉시 에스컬레이션 해제 | 어떤 단계에서든 체크인하면 종료 |
| BR-003 | 리마인더 발송 시간 | 체크인 예정 30분 전 + 예정 시간 도래 시 |
| BR-004 | SMS 재시도 정책 | 최대 3회, 5초/30초/2분 간격 |
| BR-005 | 임시 비활성 최대 기간 | 최대 30일, 이후 자동 활성화 |
| BR-006 | 이상 감지 분석 기간 | 최근 30일 체크인 데이터 기반 |
| BR-007 | 이상 감지 최소 데이터 | 최소 14일 이상 체크인 이력 보유 사용자만 분석 |
| BR-008 | 야간 알림 제한 | 22:00~08:00 사이 SMS 발송 금지 (CRITICAL 제외) |
| BR-009 | 체크인 간격 최소값 | 최소 12시간, 최대 48시간 |

---

## API 엔드포인트 명세

### PUT /api/users/me/checkin-schedule
체크인 스케줄 설정

| 항목 | 내용 |
|------|------|
| **인증** | JWT 필요 (ROLE_USER) |
| **Content-Type** | application/json |

**Request Body:**
```json
{
  "checkIntervalHours": 24,
  "preferredStartTime": "09:00",
  "preferredEndTime": "11:00",
  "activeDays": ["MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"],
  "smsReminderEnabled": true,
  "phoneNumber": "010-1234-5678"
}
```

**Response 200:**
```json
{
  "status": "OK",
  "message": "체크인 스케줄이 업데이트되었습니다",
  "data": {
    "checkIntervalHours": 24,
    "preferredStartTime": "09:00",
    "preferredEndTime": "11:00",
    "activeDays": ["MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"],
    "smsReminderEnabled": true,
    "nextCheckinDue": "2026-03-15T09:00:00"
  }
}
```

**Response 400:**
```json
{
  "status": "BAD_REQUEST",
  "message": "체크인 간격은 12~48시간 사이여야 합니다"
}
```

---

### PUT /api/users/me/checkin-pause
체크인 임시 비활성

| 항목 | 내용 |
|------|------|
| **인증** | JWT 필요 (ROLE_USER) |
| **Content-Type** | application/json |

**Request Body:**
```json
{
  "pauseStartDate": "2026-03-15",
  "pauseEndDate": "2026-03-20",
  "reason": "TRAVEL"
}
```

**Reason enum:** `TRAVEL`, `HOSPITAL`, `FAMILY_VISIT`, `OTHER`

**Response 200:**
```json
{
  "status": "OK",
  "message": "체크인이 일시 중지되었습니다",
  "data": {
    "pauseStartDate": "2026-03-15",
    "pauseEndDate": "2026-03-20",
    "reason": "TRAVEL",
    "nextCheckinDue": "2026-03-20T09:00:00"
  }
}
```

**Response 400:**
```json
{
  "status": "BAD_REQUEST",
  "message": "임시 비활성 기간은 최대 30일입니다"
}
```

---

### DELETE /api/users/me/checkin-pause
체크인 임시 비활성 취소

| 항목 | 내용 |
|------|------|
| **인증** | JWT 필요 (ROLE_USER) |

**Response 200:**
```json
{
  "status": "OK",
  "message": "체크인이 재활성화되었습니다",
  "data": {
    "nextCheckinDue": "2026-03-15T09:00:00"
  }
}
```

---

### GET /api/users/me/escalation-status
현재 사용자의 에스컬레이션 상태 조회

| 항목 | 내용 |
|------|------|
| **인증** | JWT 필요 (ROLE_USER) |

**Response 200:**
```json
{
  "status": "OK",
  "message": "에스컬레이션 상태 조회 성공",
  "data": {
    "currentLevel": "NONE",
    "nextCheckinDue": "2026-03-15T09:00:00",
    "lastCheckinAt": "2026-03-14T09:23:00",
    "isPaused": false
  }
}
```

---

### GET /api/admin/escalations
관리자 에스컬레이션 현황 목록 조회

| 항목 | 내용 |
|------|------|
| **인증** | Basic Auth (ROLE_ADMIN) |
| **Query Parameters** | `level`: REMINDER/WARNING/DANGER/CRITICAL (optional), `page`, `size` |

**Response 200:**
```json
{
  "status": "OK",
  "message": "에스컬레이션 현황 조회 성공",
  "data": {
    "content": [
      {
        "escalationId": 1,
        "userId": 42,
        "userName": "홍길동",
        "currentLevel": "DANGER",
        "escalationStartedAt": "2026-03-14T09:00:00",
        "levelChangedAt": "2026-03-14T12:00:00",
        "lastCheckinAt": "2026-03-13T09:15:00",
        "phoneNumber": "010-****-5678",
        "address": "서울시 강남구 역삼동 123-45",
        "guardianCount": 2,
        "hasGuardians": true
      }
    ],
    "totalElements": 15,
    "totalPages": 2
  }
}
```

---

### POST /api/admin/escalations/{escalationId}/resolve
관리자 에스컬레이션 수동 해제

| 항목 | 내용 |
|------|------|
| **인증** | Basic Auth (ROLE_ADMIN) |
| **Path Parameter** | `escalationId`: 에스컬레이션 ID |
| **Content-Type** | application/json |

**Request Body:**
```json
{
  "resolutionNote": "전화 확인 완료, 본인 안전 확인",
  "resolutionType": "PHONE_CONFIRMED"
}
```

**Resolution type enum:** `PHONE_CONFIRMED`, `GUARDIAN_CONFIRMED`, `VISIT_CONFIRMED`, `FALSE_ALARM`, `OTHER`

**Response 200:**
```json
{
  "status": "OK",
  "message": "에스컬레이션이 해제되었습니다",
  "data": {
    "escalationId": 1,
    "resolvedAt": "2026-03-14T13:00:00",
    "resolvedBy": "admin@unalone.kr"
  }
}
```

---

### GET /api/admin/anomalies
AI 이상 감지 결과 조회

| 항목 | 내용 |
|------|------|
| **인증** | Basic Auth (ROLE_ADMIN) |
| **Query Parameters** | `severity`: LOW/MEDIUM/HIGH (optional), `page`, `size` |

**Response 200:**
```json
{
  "status": "OK",
  "message": "이상 감지 결과 조회 성공",
  "data": {
    "content": [
      {
        "anomalyId": "6600a1b2c3d4e5f6a7b8c9d0",
        "userId": 42,
        "userName": "홍길동",
        "severity": "HIGH",
        "anomalyType": "FREQUENCY_DECREASE",
        "description": "주간 체크인 횟수가 7회에서 2회로 감소 (71% 감소)",
        "detectedAt": "2026-03-14T02:15:00",
        "dataRange": {
          "from": "2026-02-12",
          "to": "2026-03-13"
        },
        "acknowledged": false
      }
    ],
    "totalElements": 5,
    "totalPages": 1
  }
}
```

---

### POST /api/admin/anomalies/{anomalyId}/acknowledge
이상 감지 알림 확인 처리

| 항목 | 내용 |
|------|------|
| **인증** | Basic Auth (ROLE_ADMIN) |

**Response 200:**
```json
{
  "status": "OK",
  "message": "이상 감지 알림이 확인 처리되었습니다"
}
```

---

## 데이터 모델 변경

### 신규 테이블: checkin_schedules (PostgreSQL)
```sql
CREATE TABLE checkin_schedules (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    check_interval_hours INT NOT NULL DEFAULT 24,
    preferred_start_time TIME NOT NULL DEFAULT '09:00',
    preferred_end_time TIME NOT NULL DEFAULT '11:00',
    active_days VARCHAR(50) NOT NULL DEFAULT 'MON,TUE,WED,THU,FRI,SAT,SUN',
    sms_reminder_enabled BOOLEAN NOT NULL DEFAULT false,
    phone_number VARCHAR(20),
    next_checkin_due TIMESTAMP,
    pause_start_date DATE,
    pause_end_date DATE,
    pause_reason VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 신규 테이블: escalations (PostgreSQL)
```sql
CREATE TABLE escalations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    current_level VARCHAR(20) NOT NULL DEFAULT 'REMINDER',  -- REMINDER, WARNING, DANGER, CRITICAL
    escalation_started_at TIMESTAMP NOT NULL,
    level_changed_at TIMESTAMP NOT NULL,
    resolved BOOLEAN NOT NULL DEFAULT false,
    resolved_at TIMESTAMP,
    resolved_by VARCHAR(100),
    resolution_type VARCHAR(30),
    resolution_note TEXT,
    sms_sent_count INT NOT NULL DEFAULT 0,
    guardian_notified BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_escalations_active ON escalations (user_id, resolved) WHERE resolved = false;
CREATE INDEX idx_escalations_level ON escalations (current_level) WHERE resolved = false;
```

### 신규 컬렉션: anomaly_detections (MongoDB)
```json
{
  "_id": "ObjectId",
  "userId": 42,
  "severity": "HIGH",
  "anomalyType": "FREQUENCY_DECREASE",
  "description": "주간 체크인 횟수가 7회에서 2회로 감소",
  "score": 0.85,
  "metrics": {
    "baseline": {
      "avgWeeklyCheckins": 7.0,
      "avgCheckinHour": 9.2,
      "missedCheckinRate": 0.02
    },
    "current": {
      "avgWeeklyCheckins": 2.0,
      "avgCheckinHour": 14.5,
      "missedCheckinRate": 0.45
    }
  },
  "dataRange": {
    "from": "2026-02-12",
    "to": "2026-03-13"
  },
  "acknowledged": false,
  "acknowledgedBy": null,
  "acknowledgedAt": null,
  "detectedAt": "2026-03-14T02:15:00",
  "createdAt": "2026-03-14T02:15:00"
}
```

### 신규 컬렉션: sms_logs (MongoDB)
```json
{
  "_id": "ObjectId",
  "userId": 42,
  "phoneNumber": "010-****-5678",
  "messageType": "CHECKIN_REMINDER",
  "content": "안녕하세요, 오늘의 안부 체크인을 해주세요.",
  "status": "SENT",
  "providerMessageId": "ext-msg-12345",
  "retryCount": 0,
  "errorMessage": null,
  "sentAt": "2026-03-14T08:30:00",
  "createdAt": "2026-03-14T08:30:00"
}
```

---

## Kafka 토픽

### 신규 토픽: escalation-events
```json
{
  "eventType": "ESCALATION_LEVEL_CHANGED",
  "userId": 42,
  "escalationId": 1,
  "previousLevel": "WARNING",
  "currentLevel": "DANGER",
  "timestamp": "2026-03-14T12:00:00"
}
```

### 신규 토픽: anomaly-events
```json
{
  "eventType": "ANOMALY_DETECTED",
  "userId": 42,
  "anomalyId": "6600a1b2c3d4e5f6a7b8c9d0",
  "anomalyType": "FREQUENCY_DECREASE",
  "severity": "HIGH",
  "timestamp": "2026-03-14T02:15:00"
}
```

### 기존 토픽 활용: alert-events
기존 alert-events 토픽에 에스컬레이션 관련 이벤트도 발행 (기존 소비자 호환성 유지)

---

## 수용 기준 (Acceptance Criteria)

### 리마인더
- [ ] 체크인 예정 30분 전에 앱 내 알림이 발송된다
- [ ] 체크인 예정 시간 도래 시 최종 리마인더가 발송된다
- [ ] SMS 리마인더 활성화한 사용자에게 SMS가 발송된다
- [ ] 체크인 수행 시 리마인더가 중지된다

### 에스컬레이션
- [ ] 미응답 시 REMINDER → WARNING → DANGER → CRITICAL 순서로 에스컬레이션된다
- [ ] WARNING 단계에서 보호자 전원에게 알림이 발송된다
- [ ] DANGER 단계에서 관리자 대시보드에 위험 알림이 표시된다
- [ ] CRITICAL 단계에서 사용자 주소 및 긴급 연락처 정보가 관리자에게 표시된다
- [ ] 체크인 수행 시 어떤 단계에서든 에스컬레이션이 즉시 해제된다
- [ ] 관리자가 수동으로 에스컬레이션을 해제할 수 있다

### 스케줄 설정
- [ ] 사용자가 체크인 간격(12/24/48시간)을 설정할 수 있다
- [ ] 사용자가 선호 체크인 시간대를 설정할 수 있다
- [ ] 사용자가 요일별 활성/비활성을 설정할 수 있다
- [ ] 사용자가 임시 비활성 기간을 설정할 수 있다 (최대 30일)
- [ ] 임시 비활성 기간 중에는 리마인더/에스컬레이션이 발생하지 않는다
- [ ] 임시 비활성 기간 종료 후 자동으로 체크인이 재활성화된다

### AI 이상 감지
- [ ] 최근 30일 체크인 데이터를 기반으로 이상 패턴이 감지된다
- [ ] 체크인 빈도 급감 시 HIGH severity 알림이 생성된다
- [ ] 체크인 시간대 급변 시 MEDIUM severity 알림이 생성된다
- [ ] 에스컬레이션 빈도 증가 시 HIGH severity 알림이 생성된다
- [ ] 관리자가 이상 감지 알림을 확인 처리할 수 있다
- [ ] 14일 미만의 체크인 이력을 가진 사용자는 분석에서 제외된다

### SMS 발송
- [ ] SMS 발송 실패 시 3회까지 재시도된다
- [ ] 22:00~08:00 사이에는 SMS가 발송되지 않는다 (CRITICAL 제외)
- [ ] SMS 발송 이력이 MongoDB에 기록된다

### 기존 기능 호환성
- [ ] 기존 체크인 API가 정상 작동한다
- [ ] 기존 alert-events 소비자가 정상 작동한다
- [ ] 스케줄 미설정 사용자는 기존 기본값(24시간 간격)으로 동작한다

---

## 우선순위
**P0 (필수)** - 서비스의 핵심 안전 기능. 에스컬레이션 시스템은 고독사 방지라는 서비스 핵심 가치를 직접적으로 구현하는 기능이다.

---

## 구현 순서 (권장)

| 단계 | 작업 | 담당 | 예상 기간 |
|------|------|------|----------|
| 1 | DB 스키마 생성 (checkin_schedules, escalations, MongoDB 컬렉션) | Backend | 1일 |
| 2 | 체크인 스케줄 엔티티/리포지토리/서비스 구현 | Backend | 2일 |
| 3 | 에스컬레이션 엔진 구현 (스케줄러 + 상태 머신) | Backend | 3일 |
| 4 | Kafka 토픽 생성 및 이벤트 발행/소비 구현 | Backend | 2일 |
| 5 | SMS 외부 서비스 연동 (NHN Toast / AWS SNS) | Backend | 2일 |
| 6 | 관리자 에스컬레이션 API 구현 | Backend | 1일 |
| 7 | AI 이상 감지 배치 작업 구현 | Backend | 3일 |
| 8 | 체크인 스케줄 설정 UI | Frontend | 2일 |
| 9 | 에스컬레이션 현황 관리자 대시보드 UI | Frontend | 2일 |
| 10 | 이상 감지 관리자 대시보드 UI | Frontend | 1일 |
| 11 | 통합 테스트 및 QA | QA | 3일 |

---

## 디자인팀 인터페이스 (Design Team)

### 필요한 UI 변경사항

#### 1. 체크인 스케줄 설정 페이지 (프로필 설정 내)
- 체크인 간격 선택 드롭다운 (12h / 24h / 48h)
- 선호 시간대 시간 선택기 (시작/종료)
- 요일 토글 버튼 (월~일)
- SMS 리마인더 활성화 토글 + 전화번호 입력
- 임시 비활성 설정 (날짜 범위 선택 + 사유)

#### 2. 사용자 대시보드 체크인 카드
- 다음 체크인 예정 시간 표시
- 임시 비활성 상태 표시 배지
- 에스컬레이션 진행 중 경고 표시

#### 3. 관리자 에스컬레이션 대시보드
- 에스컬레이션 레벨별 색상 코드 (REMINDER: 파랑, WARNING: 주황, DANGER: 빨강, CRITICAL: 검정/빨강 깜빡임)
- 사용자 카드: 이름, 단계, 경과 시간, 주소, 보호자 수
- 수동 해제 모달 (사유 선택 + 메모)

#### 4. 관리자 이상 감지 페이지
- severity별 목록 (HIGH 상단 고정)
- 이상 감지 상세: 기준 패턴 vs 현재 패턴 비교 차트
- 확인 처리 버튼

---

## 개발팀 인터페이스 (Development Team)

### Backend 변경사항

#### 1. 신규 클래스 (api-service)
| 패키지 | 클래스 | 역할 |
|--------|--------|------|
| `domain` | `CheckinSchedule` | 체크인 스케줄 엔티티 |
| `domain` | `Escalation` | 에스컬레이션 엔티티 |
| `domain/enums` | `EscalationLevel` | REMINDER, WARNING, DANGER, CRITICAL |
| `domain/enums` | `PauseReason` | TRAVEL, HOSPITAL, FAMILY_VISIT, OTHER |
| `repository` | `CheckinScheduleRepository` | 체크인 스케줄 JPA Repository |
| `repository` | `EscalationRepository` | 에스컬레이션 JPA Repository |
| `service` | `CheckinScheduleService` | 스케줄 관리 로직 |
| `service` | `EscalationService` | 에스컬레이션 상태 머신 + 스케줄러 |
| `service` | `SmsService` | SMS 발송 (외부 API 연동) |
| `service` | `ReminderScheduler` | @Scheduled 리마인더 발송 |
| `controller` | `CheckinScheduleController` | 스케줄 설정 API |
| `dto` | `CheckinScheduleRequest/Response` | 스케줄 DTO |
| `dto` | `EscalationResponse` | 에스컬레이션 DTO |

#### 2. 신규 클래스 (admin-service)
| 패키지 | 클래스 | 역할 |
|--------|--------|------|
| `controller` | `EscalationAdminController` | 에스컬레이션 관리 API |
| `controller` | `AnomalyAdminController` | 이상 감지 관리 API |
| `service` | `EscalationAdminService` | 에스컬레이션 조회/해제 로직 |
| `service` | `AnomalyAdminService` | 이상 감지 조회/확인 로직 |

#### 3. 신규 클래스 (event-service)
| 패키지 | 클래스 | 역할 |
|--------|--------|------|
| `consumer` | `EscalationEventConsumer` | 에스컬레이션 이벤트 소비 + MongoDB 로깅 |
| `consumer` | `AnomalyEventConsumer` | 이상 감지 이벤트 소비 + MongoDB 로깅 |
| `service` | `AnomalyDetectionService` | AI 이상 감지 배치 로직 |
| `domain` | `AnomalyDetection` | 이상 감지 MongoDB Document |
| `domain` | `SmsLog` | SMS 발송 로그 MongoDB Document |
| `repository` | `AnomalyDetectionRepository` | MongoDB Repository |
| `repository` | `SmsLogRepository` | MongoDB Repository |

#### 4. 신규 클래스 (common 모듈)
| 패키지 | 클래스 | 역할 |
|--------|--------|------|
| `event` | `EscalationEvent` | 에스컬레이션 Kafka 이벤트 DTO |
| `event` | `AnomalyEvent` | 이상 감지 Kafka 이벤트 DTO |
| `config` | `KafkaTopicConfig` | 신규 토픽 설정 추가 |

#### 5. 환경 설정 추가 (application.yml)
```yaml
sms:
  provider: nhn-toast  # 또는 aws-sns
  api-key: ${SMS_API_KEY}
  sender-number: ${SMS_SENDER_NUMBER}
  max-retry: 3
  rate-limit-per-minute: 100

escalation:
  reminder-offset-minutes: 0
  warning-offset-hours: 1
  danger-offset-hours: 3
  critical-offset-hours: 6
  quiet-hours-start: "22:00"
  quiet-hours-end: "08:00"

anomaly:
  analysis-cron: "0 0 2 * * *"  # 매일 02:00
  min-data-days: 14
  frequency-decrease-threshold: 0.5  # 50% 이상 감소 시 감지
  time-shift-threshold-hours: 3      # 평균 체크인 시간 3시간 이상 변화 시 감지
```

### Frontend 변경사항

#### 1. 신규 페이지/컴포넌트
| 경로/이름 | 유형 | 역할 |
|-----------|------|------|
| `CheckinScheduleSettings.tsx` | Component | 체크인 스케줄 설정 (프로필 페이지 내) |
| `CheckinPauseModal.tsx` | Component | 임시 비활성 설정 모달 |
| `EscalationStatus.tsx` | Component | 사용자 에스컬레이션 상태 표시 |
| `admin/escalations/page.tsx` | Page | 관리자 에스컬레이션 현황 |
| `admin/anomalies/page.tsx` | Page | 관리자 이상 감지 현황 |
| `EscalationCard.tsx` | Component | 에스컬레이션 사용자 카드 |
| `AnomalyCard.tsx` | Component | 이상 감지 결과 카드 |
| `EscalationResolveModal.tsx` | Component | 에스컬레이션 수동 해제 모달 |

#### 2. 기존 코드 변경
| 파일 | 변경 내용 |
|------|----------|
| `profile/page.tsx` | CheckinScheduleSettings 컴포넌트 추가 |
| `dashboard/page.tsx` | 다음 체크인 시간 + EscalationStatus 표시 |
| `admin/page.tsx` | 에스컬레이션 요약 카드 추가 |
| `src/types/index.ts` | CheckinSchedule, Escalation, Anomaly 타입 추가 |
| `src/lib/api.ts` | 스케줄/에스컬레이션/이상감지 API 함수 추가 |

---

## 참고 자료
- 현재 체크인 코드: `backend/api-service/` 하위 체크인 관련 Controller/Service
- 현재 알림 코드: `backend/event-service/` 하위 Kafka Consumer
- NHN Cloud Toast SMS API: https://docs.nhncloud.com/ko/Notification/SMS/
- AWS SNS SMS: https://docs.aws.amazon.com/sns/latest/dg/sms_publish-to-phone.html
