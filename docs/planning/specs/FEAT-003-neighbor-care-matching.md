# FEAT-003: 이웃 돌봄 매칭 시스템 (Neighbor Care Matching)

## 목적
현재 Unalone 서비스는 체크인 미응답 시 보호자 알림과 관리자 모니터링만 제공한다. 이웃 돌봄 매칭 시스템을 도입하여 다음 목표를 달성한다:

1. **지역 기반 안전망 구축**: 가까운 거리의 자원봉사자가 직접 방문하여 안부를 확인할 수 있는 체계 마련
2. **사회적 고립 해소**: 정기적인 이웃 방문을 통해 1인 가구의 사회적 연결 강화
3. **빠른 현장 대응**: 에스컬레이션 상황에서 보호자보다 더 빠르게 현장 확인 가능
4. **커뮤니티 활성화**: 자원봉사 참여를 통한 지역사회 유대감 형성

## 대상 사용자
- **일반 사용자 (ROLE_USER)**: 돌봄 서비스 수신 대상 (수신 동의 필요)
- **자원봉사자 (ROLE_USER + VOLUNTEER)**: 돌봄 방문을 수행하는 검증된 사용자
- **관리자 (ROLE_ADMIN)**: 자원봉사자 승인, 매칭 관리, 방문 보고서 확인

## 선행 조건
- 사용자 프로필에 주소 정보 등록 기능 (현재 완료 상태)
- 에스컬레이션 시스템 (FEAT-002) 구현 완료 또는 병행 개발
- 보호자 관리 기능 구현 완료 (현재 완료 상태)

---

## 사용자 스토리

### US-001: 자원봉사자 등록
```
AS A 서비스 사용자
I WANT TO 이웃 돌봄 자원봉사자로 등록하고 싶다
SO THAT 근처에 사는 1인 가구의 안전을 도울 수 있다
```

### US-002: 돌봄 서비스 수신 동의
```
AS A 1인 가구 사용자
I WANT TO 이웃 돌봄 서비스를 수신하겠다고 동의하고 싶다
SO THAT 위험 상황에서 가까운 자원봉사자가 방문하여 안부를 확인해줄 수 있다
```

### US-003: 돌봄 방문 수행
```
AS A 자원봉사자
I WANT TO 매칭된 대상자에게 돌봄 방문을 수행하고 결과를 보고하고 싶다
SO THAT 대상자의 안전을 확인하고 서비스에 기여할 수 있다
```

### US-004: 돌봄 매칭 관리
```
AS A 관리자
I WANT TO 자원봉사자와 대상자의 매칭 현황을 관리하고 싶다
SO THAT 돌봄 서비스가 효율적으로 운영될 수 있다
```

### US-005: 활동 평가
```
AS A 돌봄 서비스 수신자
I WANT TO 자원봉사자의 방문에 대해 평가를 남기고 싶다
SO THAT 서비스 품질이 유지되고 좋은 자원봉사자가 인정받을 수 있다
```

---

## 기능 상세

### 1. 주요 흐름 (Happy Path)

#### 1-1. 자원봉사자 등록 및 승인
```
1. 기존 사용자가 자원봉사자 등록 페이지에서 신청
2. 필수 정보 입력:
   - 활동 가능 지역 (주소 기반 반경 설정)
   - 활동 가능 시간대
   - 활동 가능 요일
   - 본인 인증 (휴대폰 인증)
   - 간단한 자기소개
3. 관리자가 신청 내역 검토 후 승인/반려
4. 승인 시 사용자 역할에 VOLUNTEER 태그 추가
5. 자원봉사자 프로필 활성화
```

#### 1-2. 돌봄 대상자 등록 (수신 동의)
```
1. 사용자가 프로필 설정에서 "이웃 돌봄 서비스 수신 동의" 활성화
2. 주소 정보 확인 (미등록 시 등록 유도)
3. 방문 허용 시간대 설정
4. 비상 연락처 확인
5. 개인정보 공유 범위 설정 (이름, 주소 동/호 단위만, 연락처 등)
6. 서비스 활성화
```

#### 1-3. 자동 매칭
```
1. 시스템이 대상자 주소 기반 반경 2km 이내 활동 가능 자원봉사자 탐색
2. 매칭 우선순위:
   a. 거리 (가까울수록 우선)
   b. 활동 시간대 겹침
   c. 신뢰 점수 (높을수록 우선)
   d. 현재 담당 대상자 수 (적을수록 우선, 최대 5명)
3. 매칭 후보 생성 → 관리자 확인 → 매칭 확정
4. 자원봉사자와 대상자 양쪽에 매칭 알림
5. 자원봉사자에게 대상자 기본 정보 공유 (동의 범위 내)
```

#### 1-4. 돌봄 방문 수행
```
1. 정기 방문: 매칭 시 설정한 주기에 따라 방문 일정 자동 생성
2. 긴급 방문: 에스컬레이션 DANGER 이상 시 매칭된 자원봉사자에게 방문 요청
3. 자원봉사자가 방문 수행
4. 방문 완료 후 보고서 작성:
   - 방문 시간
   - 대상자 상태 (양호/주의/위험)
   - 특이사항 메모
   - 방문 사진 (선택, 대상자 동의 필요)
5. 보고서 제출 → 관리자 대시보드에 표시
6. care-visit-events Kafka 토픽에 이벤트 발행
```

#### 1-5. 신뢰 점수 / 활동 평가
```
1. 방문 완료 후 대상자가 자원봉사자에 대해 평가 (1~5점 + 코멘트)
2. 신뢰 점수 계산:
   - 기본 점수: 50점
   - 방문 완료: +2점/회
   - 긍정 평가 (4~5점): +3점
   - 부정 평가 (1~2점): -5점
   - 방문 미이행: -10점
   - 보고서 미제출: -3점
   - 최대 100점, 최소 0점
3. 신뢰 점수 30점 미만 시 자원봉사 자격 자동 정지
4. 월간 활동 통계 제공 (방문 횟수, 평균 평점)
```

### 2. 예외 흐름 (Edge Cases)

#### 2-1. 매칭 가능한 자원봉사자 없음
```
- 조건: 대상자 반경 2km 이내에 활동 가능 자원봉사자가 없음
- 처리: 반경을 5km로 확대하여 재탐색, 그래도 없으면 관리자에게 알림
- 대상자에게: "현재 지역에 활동 가능한 자원봉사자를 찾고 있습니다" 안내
```

#### 2-2. 자원봉사자가 긴급 방문 요청 거절/미응답
```
- 조건: 에스컬레이션 기반 긴급 방문 요청에 30분 내 응답 없음
- 처리: 다음 순위 자원봉사자에게 요청 전환, 모든 자원봉사자 미응답 시 관리자 알림
- 타임아웃: 긴급 방문 요청은 30분 내 수락 필요
```

#### 2-3. 대상자가 방문 거부
```
- 조건: 자원봉사자 방문 시 대상자가 방문을 거부
- 처리: 자원봉사자가 "방문 거부" 상태로 보고서 작성, 신뢰 점수 감점 없음
- 반복 거부 시: 3회 연속 거부 시 관리자에게 알림, 서비스 수신 동의 재확인
```

#### 2-4. 대상자가 부재 (문 앞 미응답)
```
- 조건: 자원봉사자가 방문했으나 대상자가 응답하지 않음
- 처리: "부재" 상태로 보고서 작성 → 에스컬레이션 레벨 상향 트리거
- 추가 조치: 보호자에게 즉시 알림 발송
```

#### 2-5. 자원봉사자 자격 정지
```
- 조건: 신뢰 점수 30점 미만 도달
- 처리: 자원봉사 자격 자동 정지, 담당 대상자 재매칭 필요
- 복구: 관리자 검토 후 수동 복구 가능
```

#### 2-6. 개인정보 보호
```
- 조건: 자원봉사자에게 대상자 정보 공유 시
- 처리: 대상자가 설정한 공유 범위만 전달
- 기본값: 이름(성만), 주소(동 단위만), 방문 허용 시간
- 상세 주소: 방문 수락 후에만 공개 (호수 단위)
```

### 3. 비즈니스 규칙

| 규칙 ID | 규칙 | 설명 |
|---------|------|------|
| BR-001 | 자원봉사자 사전 승인 필수 | 관리자 승인 없이 활동 불가 |
| BR-002 | 매칭 반경 기본 2km | 없을 시 5km로 확대 |
| BR-003 | 자원봉사자 최대 담당 5명 | 과부하 방지 |
| BR-004 | 긴급 방문 요청 타임아웃 30분 | 미응답 시 다음 자원봉사자에게 전달 |
| BR-005 | 신뢰 점수 0~100점 | 30점 미만 시 자격 정지 |
| BR-006 | 대상자 동의 필수 | 돌봄 서비스 수신 명시적 동의 |
| BR-007 | 정기 방문 주기 | 주 1~3회, 매칭 시 설정 |
| BR-008 | 방문 보고서 24시간 내 제출 | 미제출 시 신뢰 점수 감점 |
| BR-009 | 개인정보 최소 공개 | 동의 범위 내에서만 정보 공유 |
| BR-010 | 방문 거부 3회 연속 시 서비스 재확인 | 관리자가 대상자에게 연락 |

---

## API 엔드포인트 명세

### POST /api/volunteers/register
자원봉사자 등록 신청

| 항목 | 내용 |
|------|------|
| **인증** | JWT 필요 (ROLE_USER) |
| **Content-Type** | application/json |

**Request Body:**
```json
{
  "activityRadius": 2.0,
  "activityAddress": "서울시 강남구 역삼동",
  "activityLatitude": 37.5000,
  "activityLongitude": 127.0360,
  "availableTimeStart": "09:00",
  "availableTimeEnd": "18:00",
  "availableDays": ["MON", "WED", "FRI", "SAT"],
  "phoneNumber": "010-1234-5678",
  "introduction": "근처에 사는 직장인입니다. 주말에 활동 가능합니다."
}
```

**Response 201:**
```json
{
  "status": "CREATED",
  "message": "자원봉사자 등록이 신청되었습니다. 관리자 승인 후 활동 가능합니다.",
  "data": {
    "volunteerId": 1,
    "status": "PENDING",
    "appliedAt": "2026-03-14T10:00:00"
  }
}
```

**Response 409:**
```json
{
  "status": "CONFLICT",
  "message": "이미 자원봉사자로 등록되어 있습니다"
}
```

---

### GET /api/volunteers/me
자원봉사자 내 정보 조회

| 항목 | 내용 |
|------|------|
| **인증** | JWT 필요 (ROLE_USER + VOLUNTEER) |

**Response 200:**
```json
{
  "status": "OK",
  "message": "자원봉사자 정보 조회 성공",
  "data": {
    "volunteerId": 1,
    "status": "ACTIVE",
    "trustScore": 72,
    "totalVisits": 15,
    "averageRating": 4.5,
    "currentMatchCount": 3,
    "maxMatchCount": 5,
    "activityRadius": 2.0,
    "availableTimeStart": "09:00",
    "availableTimeEnd": "18:00",
    "availableDays": ["MON", "WED", "FRI", "SAT"],
    "monthlyStats": {
      "visits": 6,
      "averageRating": 4.7,
      "noShows": 0
    }
  }
}
```

---

### PUT /api/volunteers/me
자원봉사자 정보 수정

| 항목 | 내용 |
|------|------|
| **인증** | JWT 필요 (ROLE_USER + VOLUNTEER) |
| **Content-Type** | application/json |

**Request Body:**
```json
{
  "activityRadius": 3.0,
  "availableTimeStart": "10:00",
  "availableTimeEnd": "20:00",
  "availableDays": ["SAT", "SUN"]
}
```

**Response 200:**
```json
{
  "status": "OK",
  "message": "자원봉사자 정보가 수정되었습니다"
}
```

---

### POST /api/users/me/care-consent
돌봄 서비스 수신 동의

| 항목 | 내용 |
|------|------|
| **인증** | JWT 필요 (ROLE_USER) |
| **Content-Type** | application/json |

**Request Body:**
```json
{
  "consentGiven": true,
  "allowedVisitTimeStart": "10:00",
  "allowedVisitTimeEnd": "20:00",
  "allowedVisitDays": ["MON", "TUE", "WED", "THU", "FRI"],
  "shareNameLevel": "LAST_NAME_ONLY",
  "shareAddressLevel": "DONG_ONLY",
  "sharePhone": false,
  "visitFrequency": "WEEKLY"
}
```

**Share level enum:** `FULL`, `LAST_NAME_ONLY`, `NONE` (이름), `FULL_ADDRESS`, `DONG_ONLY`, `NONE` (주소)
**Visit frequency enum:** `WEEKLY`, `TWICE_WEEKLY`, `THREE_TIMES_WEEKLY`

**Response 200:**
```json
{
  "status": "OK",
  "message": "돌봄 서비스 수신이 동의되었습니다",
  "data": {
    "careConsentId": 1,
    "consentGiven": true,
    "consentedAt": "2026-03-14T10:00:00"
  }
}
```

---

### GET /api/volunteers/me/matches
자원봉사자 매칭 목록 조회

| 항목 | 내용 |
|------|------|
| **인증** | JWT 필요 (ROLE_USER + VOLUNTEER) |

**Response 200:**
```json
{
  "status": "OK",
  "message": "매칭 목록 조회 성공",
  "data": [
    {
      "matchId": 1,
      "careReceiver": {
        "displayName": "김*",
        "areaName": "역삼동",
        "distanceKm": 0.8,
        "allowedVisitTimeStart": "10:00",
        "allowedVisitTimeEnd": "20:00"
      },
      "status": "ACTIVE",
      "visitFrequency": "WEEKLY",
      "nextScheduledVisit": "2026-03-16T14:00:00",
      "totalVisits": 5,
      "lastVisitAt": "2026-03-09T14:30:00"
    }
  ]
}
```

---

### POST /api/care-visits
돌봄 방문 보고서 작성

| 항목 | 내용 |
|------|------|
| **인증** | JWT 필요 (ROLE_USER + VOLUNTEER) |
| **Content-Type** | application/json |

**Request Body:**
```json
{
  "matchId": 1,
  "visitType": "REGULAR",
  "visitedAt": "2026-03-14T14:30:00",
  "duration": 30,
  "receiverStatus": "GOOD",
  "notes": "건강 상태 양호, 식사 잘 하고 계심",
  "refusedByReceiver": false
}
```

**Visit type enum:** `REGULAR`, `EMERGENCY`, `FOLLOWUP`
**Receiver status enum:** `GOOD`, `CAUTION`, `DANGER`, `ABSENT`, `REFUSED`

**Response 201:**
```json
{
  "status": "CREATED",
  "message": "방문 보고서가 등록되었습니다",
  "data": {
    "visitId": 1,
    "reportedAt": "2026-03-14T15:00:00"
  }
}
```

---

### POST /api/care-visits/{visitId}/rate
방문 평가 (대상자)

| 항목 | 내용 |
|------|------|
| **인증** | JWT 필요 (ROLE_USER, 대상자 본인) |
| **Content-Type** | application/json |

**Request Body:**
```json
{
  "rating": 5,
  "comment": "친절하게 방문해 주셔서 감사합니다"
}
```

**Response 200:**
```json
{
  "status": "OK",
  "message": "방문 평가가 등록되었습니다"
}
```

---

### GET /api/admin/volunteers
관리자 자원봉사자 목록 조회

| 항목 | 내용 |
|------|------|
| **인증** | Basic Auth (ROLE_ADMIN) |
| **Query Parameters** | `status`: PENDING/ACTIVE/SUSPENDED (optional), `page`, `size` |

**Response 200:**
```json
{
  "status": "OK",
  "message": "자원봉사자 목록 조회 성공",
  "data": {
    "content": [
      {
        "volunteerId": 1,
        "userId": 10,
        "userName": "이봉사",
        "status": "PENDING",
        "activityArea": "서울시 강남구 역삼동",
        "trustScore": 50,
        "appliedAt": "2026-03-14T10:00:00",
        "currentMatchCount": 0
      }
    ],
    "totalElements": 10,
    "totalPages": 1
  }
}
```

---

### PUT /api/admin/volunteers/{volunteerId}/approve
자원봉사자 승인

| 항목 | 내용 |
|------|------|
| **인증** | Basic Auth (ROLE_ADMIN) |

**Response 200:**
```json
{
  "status": "OK",
  "message": "자원봉사자가 승인되었습니다"
}
```

---

### PUT /api/admin/volunteers/{volunteerId}/reject
자원봉사자 반려

| 항목 | 내용 |
|------|------|
| **인증** | Basic Auth (ROLE_ADMIN) |
| **Content-Type** | application/json |

**Request Body:**
```json
{
  "reason": "활동 지역 정보가 불충분합니다. 보다 구체적인 주소를 기재해주세요."
}
```

**Response 200:**
```json
{
  "status": "OK",
  "message": "자원봉사자 신청이 반려되었습니다"
}
```

---

### GET /api/admin/care-matches
관리자 매칭 현황 조회

| 항목 | 내용 |
|------|------|
| **인증** | Basic Auth (ROLE_ADMIN) |
| **Query Parameters** | `status`: PENDING/ACTIVE/TERMINATED (optional), `page`, `size` |

**Response 200:**
```json
{
  "status": "OK",
  "message": "매칭 현황 조회 성공",
  "data": {
    "content": [
      {
        "matchId": 1,
        "volunteer": {
          "volunteerId": 1,
          "userName": "이봉사",
          "trustScore": 72
        },
        "careReceiver": {
          "userId": 42,
          "userName": "김수신",
          "area": "역삼동"
        },
        "status": "ACTIVE",
        "visitFrequency": "WEEKLY",
        "totalVisits": 5,
        "lastVisitAt": "2026-03-09T14:30:00",
        "averageRating": 4.5,
        "matchedAt": "2026-02-01T10:00:00"
      }
    ],
    "totalElements": 25,
    "totalPages": 3
  }
}
```

---

### GET /api/admin/care-visits
관리자 방문 보고서 조회

| 항목 | 내용 |
|------|------|
| **인증** | Basic Auth (ROLE_ADMIN) |
| **Query Parameters** | `receiverStatus`: GOOD/CAUTION/DANGER/ABSENT/REFUSED (optional), `page`, `size` |

**Response 200:**
```json
{
  "status": "OK",
  "message": "방문 보고서 조회 성공",
  "data": {
    "content": [
      {
        "visitId": 1,
        "matchId": 1,
        "volunteerName": "이봉사",
        "receiverName": "김수신",
        "visitType": "REGULAR",
        "visitedAt": "2026-03-14T14:30:00",
        "duration": 30,
        "receiverStatus": "GOOD",
        "notes": "건강 상태 양호",
        "rating": 5,
        "reportedAt": "2026-03-14T15:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 10
  }
}
```

---

## 데이터 모델 변경

### 신규 테이블: volunteers (PostgreSQL)
```sql
CREATE TABLE volunteers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, ACTIVE, SUSPENDED, REJECTED
    activity_address VARCHAR(255) NOT NULL,
    activity_latitude DECIMAL(10, 7) NOT NULL,
    activity_longitude DECIMAL(10, 7) NOT NULL,
    activity_radius DECIMAL(3, 1) NOT NULL DEFAULT 2.0,
    available_time_start TIME NOT NULL,
    available_time_end TIME NOT NULL,
    available_days VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    introduction TEXT,
    trust_score INT NOT NULL DEFAULT 50,
    total_visits INT NOT NULL DEFAULT 0,
    total_rating_sum INT NOT NULL DEFAULT 0,
    total_rating_count INT NOT NULL DEFAULT 0,
    approved_at TIMESTAMP,
    approved_by VARCHAR(100),
    rejected_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_volunteers_status ON volunteers (status);
CREATE INDEX idx_volunteers_location ON volunteers (activity_latitude, activity_longitude);
```

### 신규 테이블: care_consents (PostgreSQL)
```sql
CREATE TABLE care_consents (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    consent_given BOOLEAN NOT NULL DEFAULT false,
    allowed_visit_time_start TIME,
    allowed_visit_time_end TIME,
    allowed_visit_days VARCHAR(50),
    share_name_level VARCHAR(20) NOT NULL DEFAULT 'LAST_NAME_ONLY',
    share_address_level VARCHAR(20) NOT NULL DEFAULT 'DONG_ONLY',
    share_phone BOOLEAN NOT NULL DEFAULT false,
    visit_frequency VARCHAR(20) NOT NULL DEFAULT 'WEEKLY',
    consented_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 신규 테이블: care_matches (PostgreSQL)
```sql
CREATE TABLE care_matches (
    id BIGSERIAL PRIMARY KEY,
    volunteer_id BIGINT NOT NULL,
    receiver_user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, ACTIVE, TERMINATED
    visit_frequency VARCHAR(20) NOT NULL DEFAULT 'WEEKLY',
    distance_km DECIMAL(5, 2),
    next_scheduled_visit TIMESTAMP,
    total_visits INT NOT NULL DEFAULT 0,
    matched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    terminated_at TIMESTAMP,
    termination_reason TEXT,
    FOREIGN KEY (volunteer_id) REFERENCES volunteers(id),
    FOREIGN KEY (receiver_user_id) REFERENCES users(id),
    UNIQUE (volunteer_id, receiver_user_id)
);

CREATE INDEX idx_care_matches_status ON care_matches (status);
CREATE INDEX idx_care_matches_volunteer ON care_matches (volunteer_id, status);
CREATE INDEX idx_care_matches_receiver ON care_matches (receiver_user_id, status);
```

### 신규 테이블: care_visits (PostgreSQL)
```sql
CREATE TABLE care_visits (
    id BIGSERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL,
    visit_type VARCHAR(20) NOT NULL DEFAULT 'REGULAR',  -- REGULAR, EMERGENCY, FOLLOWUP
    visited_at TIMESTAMP NOT NULL,
    duration INT,  -- 방문 시간 (분)
    receiver_status VARCHAR(20) NOT NULL,  -- GOOD, CAUTION, DANGER, ABSENT, REFUSED
    notes TEXT,
    refused_by_receiver BOOLEAN NOT NULL DEFAULT false,
    rating INT,           -- 1~5, 대상자가 평가
    rating_comment TEXT,
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (match_id) REFERENCES care_matches(id)
);

CREATE INDEX idx_care_visits_match ON care_visits (match_id);
CREATE INDEX idx_care_visits_status ON care_visits (receiver_status);
```

### 신규 컬렉션: care_visit_logs (MongoDB)
```json
{
  "_id": "ObjectId",
  "visitId": 1,
  "matchId": 1,
  "volunteerId": 1,
  "receiverUserId": 42,
  "visitType": "REGULAR",
  "receiverStatus": "GOOD",
  "eventType": "VISIT_COMPLETED",
  "metadata": {
    "distanceKm": 0.8,
    "duration": 30,
    "escalationTriggered": false
  },
  "createdAt": "2026-03-14T15:00:00"
}
```

---

## Kafka 토픽

### 신규 토픽: care-visit-events
```json
{
  "eventType": "VISIT_COMPLETED",
  "visitId": 1,
  "matchId": 1,
  "volunteerId": 1,
  "receiverUserId": 42,
  "receiverStatus": "GOOD",
  "visitType": "REGULAR",
  "timestamp": "2026-03-14T15:00:00"
}
```

### 신규 토픽: care-match-events
```json
{
  "eventType": "MATCH_CREATED",
  "matchId": 1,
  "volunteerId": 1,
  "receiverUserId": 42,
  "timestamp": "2026-03-14T10:00:00"
}
```

### 기존 토픽 연동: escalation-events (FEAT-002)
에스컬레이션 DANGER 이상 이벤트 수신 시 매칭된 자원봉사자에게 긴급 방문 요청 트리거

---

## 수용 기준 (Acceptance Criteria)

### 자원봉사자 등록
- [ ] 사용자가 자원봉사자로 등록 신청할 수 있다
- [ ] 활동 지역, 시간대, 요일, 전화번호, 자기소개를 입력할 수 있다
- [ ] 관리자가 자원봉사자 신청을 승인할 수 있다
- [ ] 관리자가 자원봉사자 신청을 반려할 수 있다 (사유 포함)
- [ ] 승인된 자원봉사자만 매칭 및 방문 활동이 가능하다
- [ ] 이미 등록된 사용자의 중복 등록이 방지된다

### 돌봄 서비스 수신
- [ ] 사용자가 돌봄 서비스 수신에 동의할 수 있다
- [ ] 방문 허용 시간대와 요일을 설정할 수 있다
- [ ] 개인정보 공유 범위를 설정할 수 있다
- [ ] 동의를 철회할 수 있다

### 매칭
- [ ] 대상자 반경 2km 이내의 자원봉사자가 자동 탐색된다
- [ ] 매칭 후보가 거리, 시간대 겹침, 신뢰 점수 기준으로 정렬된다
- [ ] 자원봉사자의 최대 담당 인원(5명)이 초과되지 않는다
- [ ] 매칭 확정 시 양쪽에 알림이 발송된다
- [ ] 자원봉사자에게는 동의 범위 내의 정보만 공유된다

### 방문 보고
- [ ] 자원봉사자가 방문 보고서를 작성할 수 있다
- [ ] 대상자 상태(양호/주의/위험/부재/거부)를 기록할 수 있다
- [ ] 보고서가 24시간 내에 제출되지 않으면 신뢰 점수가 감점된다
- [ ] 대상자 부재 시 에스컬레이션이 상향 트리거된다

### 평가 및 신뢰 점수
- [ ] 대상자가 방문에 대해 1~5점 평가를 할 수 있다
- [ ] 신뢰 점수가 규칙에 따라 자동 계산된다
- [ ] 신뢰 점수 30점 미만 시 자원봉사 자격이 자동 정지된다
- [ ] 월간 활동 통계가 자원봉사자에게 제공된다

### 관리자 기능
- [ ] 관리자가 자원봉사자 목록을 상태별로 조회할 수 있다
- [ ] 관리자가 매칭 현황을 조회할 수 있다
- [ ] 관리자가 방문 보고서를 조회할 수 있다
- [ ] 관리자가 매칭을 수동으로 종료할 수 있다

### 보안 및 개인정보
- [ ] 대상자의 상세 주소는 방문 수락 후에만 자원봉사자에게 공개된다
- [ ] 동의 범위를 벗어난 정보가 노출되지 않는다
- [ ] 자원봉사자 등록 시 휴대폰 인증이 수행된다

---

## 우선순위
**P1 (중요)** - 서비스의 사회적 안전망 확장을 위한 핵심 기능. FEAT-002(에스컬레이션) 이후 개발 우선순위.

---

## 구현 순서 (권장)

| 단계 | 작업 | 담당 | 예상 기간 |
|------|------|------|----------|
| 1 | DB 스키마 생성 (volunteers, care_consents, care_matches, care_visits) | Backend | 1일 |
| 2 | 자원봉사자 등록/수정 엔티티/리포지토리/서비스 구현 | Backend | 2일 |
| 3 | 돌봄 수신 동의 API 구현 | Backend | 1일 |
| 4 | 매칭 알고리즘 구현 (위치 기반 탐색 + 점수 계산) | Backend | 3일 |
| 5 | 방문 보고서 CRUD API 구현 | Backend | 2일 |
| 6 | 평가 및 신뢰 점수 시스템 구현 | Backend | 2일 |
| 7 | 관리자 API (자원봉사자 승인, 매칭/방문 조회) 구현 | Backend | 2일 |
| 8 | Kafka 이벤트 발행/소비 구현 | Backend | 1일 |
| 9 | 에스컬레이션 연동 (긴급 방문 요청) | Backend | 2일 |
| 10 | 자원봉사자 등록/관리 UI | Frontend | 3일 |
| 11 | 돌봄 수신 동의 설정 UI | Frontend | 1일 |
| 12 | 매칭 현황 / 방문 보고서 UI (자원봉사자 + 관리자) | Frontend | 3일 |
| 13 | 통합 테스트 및 QA | QA | 3일 |

---

## 디자인팀 인터페이스 (Design Team)

### 필요한 UI 변경사항

#### 1. 자원봉사자 등록 페이지 (`/volunteer/register`)
- 등록 폼: 활동 지역 지도 선택, 반경 슬라이더, 시간대/요일 선택
- 자기소개 텍스트 입력
- 약관 동의 체크박스

#### 2. 자원봉사자 대시보드 (`/volunteer/dashboard`)
- 내 매칭 목록 카드 (대상자별 기본 정보, 다음 방문 예정일)
- 이번 달 활동 통계 (방문 횟수, 평균 평점, 신뢰 점수)
- 긴급 방문 요청 알림 배너

#### 3. 방문 보고서 작성 페이지
- 대상자 상태 선택 (아이콘 + 컬러)
- 특이사항 메모 입력
- 방문 시간/소요시간 입력

#### 4. 돌봄 수신 동의 설정 (프로필 설정 내)
- 동의/해제 토글
- 방문 허용 시간대/요일 설정
- 개인정보 공유 범위 설정 (시각적 프라이버시 레벨)

#### 5. 관리자 돌봄 관리 페이지
- 자원봉사자 승인 대기 목록
- 매칭 현황 테이블
- 방문 보고서 목록 (상태별 필터, 위험/주의 하이라이트)
- 신뢰 점수 분포 차트

---

## 개발팀 인터페이스 (Development Team)

### Backend 변경사항

#### 1. 신규 클래스 (api-service)
| 패키지 | 클래스 | 역할 |
|--------|--------|------|
| `domain` | `Volunteer` | 자원봉사자 엔티티 |
| `domain` | `CareConsent` | 돌봄 수신 동의 엔티티 |
| `domain` | `CareMatch` | 돌봄 매칭 엔티티 |
| `domain` | `CareVisit` | 방문 보고서 엔티티 |
| `domain/enums` | `VolunteerStatus` | PENDING, ACTIVE, SUSPENDED, REJECTED |
| `domain/enums` | `MatchStatus` | PENDING, ACTIVE, TERMINATED |
| `domain/enums` | `VisitType` | REGULAR, EMERGENCY, FOLLOWUP |
| `domain/enums` | `ReceiverStatus` | GOOD, CAUTION, DANGER, ABSENT, REFUSED |
| `domain/enums` | `ShareLevel` | FULL, LAST_NAME_ONLY, NONE |
| `repository` | `VolunteerRepository` | 자원봉사자 JPA Repository (위치 기반 쿼리 포함) |
| `repository` | `CareConsentRepository` | 돌봄 동의 JPA Repository |
| `repository` | `CareMatchRepository` | 매칭 JPA Repository |
| `repository` | `CareVisitRepository` | 방문 JPA Repository |
| `service` | `VolunteerService` | 자원봉사자 등록/수정/조회 |
| `service` | `CareMatchingService` | 매칭 알고리즘 + 관리 |
| `service` | `CareVisitService` | 방문 보고 + 평가 |
| `service` | `TrustScoreService` | 신뢰 점수 계산 |
| `controller` | `VolunteerController` | 자원봉사자 API |
| `controller` | `CareConsentController` | 돌봄 동의 API |
| `controller` | `CareVisitController` | 방문 보고 API |

#### 2. 신규 클래스 (admin-service)
| 패키지 | 클래스 | 역할 |
|--------|--------|------|
| `controller` | `VolunteerAdminController` | 자원봉사자 관리 API |
| `controller` | `CareMatchAdminController` | 매칭 관리 API |
| `controller` | `CareVisitAdminController` | 방문 보고서 관리 API |

#### 3. 신규 클래스 (event-service)
| 패키지 | 클래스 | 역할 |
|--------|--------|------|
| `consumer` | `CareVisitEventConsumer` | 방문 이벤트 소비 + MongoDB 로깅 |
| `consumer` | `CareMatchEventConsumer` | 매칭 이벤트 소비 + MongoDB 로깅 |
| `service` | `EmergencyVisitTriggerService` | 에스컬레이션 이벤트 수신 시 긴급 방문 요청 |

#### 4. 신규 클래스 (common 모듈)
| 패키지 | 클래스 | 역할 |
|--------|--------|------|
| `event` | `CareVisitEvent` | 방문 Kafka 이벤트 DTO |
| `event` | `CareMatchEvent` | 매칭 Kafka 이벤트 DTO |

#### 5. 위치 기반 검색 참고사항
- PostgreSQL의 `earthdistance` 확장 또는 Haversine 공식 사용
- 쿼리 예시: `SELECT * FROM volunteers WHERE earth_distance(ll_to_earth(lat, lng), ll_to_earth(?, ?)) < ? * 1000`
- 대안: PostGIS 확장 사용

### Frontend 변경사항

#### 1. 신규 페이지/컴포넌트
| 경로/이름 | 유형 | 역할 |
|-----------|------|------|
| `volunteer/register/page.tsx` | Page | 자원봉사자 등록 |
| `volunteer/dashboard/page.tsx` | Page | 자원봉사자 대시보드 |
| `volunteer/visit-report/page.tsx` | Page | 방문 보고서 작성 |
| `CareConsentSettings.tsx` | Component | 돌봄 수신 동의 설정 |
| `MatchCard.tsx` | Component | 매칭 정보 카드 |
| `VisitReportForm.tsx` | Component | 방문 보고서 폼 |
| `RatingForm.tsx` | Component | 방문 평가 폼 |
| `admin/volunteers/page.tsx` | Page | 관리자 자원봉사자 관리 |
| `admin/care-matches/page.tsx` | Page | 관리자 매칭 현황 |
| `admin/care-visits/page.tsx` | Page | 관리자 방문 보고서 |

#### 2. 기존 코드 변경
| 파일 | 변경 내용 |
|------|----------|
| `profile/page.tsx` | CareConsentSettings 컴포넌트 추가 |
| `src/types/index.ts` | Volunteer, CareMatch, CareVisit 등 타입 추가 |
| `src/lib/api.ts` | 자원봉사/매칭/방문 API 함수 추가 |
| 네비게이션 | 자원봉사 메뉴 추가 |
| `admin/page.tsx` | 돌봄 관리 요약 카드 추가 |

---

## 참고 자료
- 현재 사용자 프로필 코드: `backend/api-service/` 하위 Profile 관련 Controller/Service
- PostGIS 문서: https://postgis.net/documentation/
- PostgreSQL earthdistance: https://www.postgresql.org/docs/current/earthdistance.html
- FEAT-002 에스컬레이션 시스템과의 연동 필요
