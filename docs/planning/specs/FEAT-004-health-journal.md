# FEAT-004: 건강 일지 & 감정 추적 (Health Journal & Mood Tracking)

## 목적
현재 체크인 시스템은 단순한 "생존 확인"만 수행하며, 사용자의 건강 상태나 감정 변화에 대한 데이터를 수집하지 않는다. 건강 일지 및 감정 추적 기능을 도입하여 다음 목표를 달성한다:

1. **체크인 데이터 풍부화**: 단순 체크인을 넘어 건강/감정 데이터를 함께 기록하여 사용자 상태를 입체적으로 파악
2. **추세 분석 및 시각화**: 시간에 따른 건강/감정 변화를 차트로 시각화하여 사용자 자기 인식 향상
3. **건강 악화 조기 감지**: 감정 점수 하락, 수면/식사 패턴 변화 등을 감지하여 선제적 개입
4. **AI 이상 감지 연동**: FEAT-002 이상 감지 시스템에 건강 데이터를 통합하여 정확도 향상

## 대상 사용자
- **일반 사용자 (ROLE_USER)**: 건강 일지 작성 및 추세 확인
- **관리자 (ROLE_ADMIN)**: 건강 악화 알림 수신, 전체 사용자 건강 추세 모니터링

## 선행 조건
- 체크인 시스템 구현 완료 (현재 완료 상태)
- 체크인 이력 조회 기능 (현재 완료 상태)
- FEAT-002 AI 이상 감지 시스템 (연동 시 필요, 독립 개발 가능)

---

## 사용자 스토리

### US-001: 체크인 시 감정/건강 기록
```
AS A 1인 가구 사용자
I WANT TO 체크인할 때 오늘의 기분과 건강 상태를 간단히 기록하고 싶다
SO THAT 나의 상태 변화를 추적하고 건강 관리에 도움을 받을 수 있다
```

### US-002: 건강 일지 작성
```
AS A 1인 가구 사용자
I WANT TO 수면, 식사, 운동, 복약 등 일상 건강 정보를 기록하고 싶다
SO THAT 규칙적인 생활 습관을 유지하고 이상 징후를 조기에 파악할 수 있다
```

### US-003: 추세 시각화 확인
```
AS A 1인 가구 사용자
I WANT TO 최근 감정과 건강 상태의 변화 추이를 차트로 확인하고 싶다
SO THAT 나의 상태 변화를 객관적으로 파악할 수 있다
```

### US-004: 건강 악화 감지 알림
```
AS A 관리자
I WANT TO 사용자의 감정/건강 점수가 지속적으로 하락하는 경우 알림을 받고 싶다
SO THAT 적절한 지원이나 개입을 제공할 수 있다
```

### US-005: 커뮤니티 건강 인사이트
```
AS A 1인 가구 사용자
I WANT TO 나와 비슷한 연령대의 평균 건강 점수와 비교해보고 싶다
SO THAT 내 상태가 어느 수준인지 가늠할 수 있다
```

---

## 기능 상세

### 1. 주요 흐름 (Happy Path)

#### 1-1. 체크인 시 감정/건강 간편 기록
```
1. 사용자가 체크인 버튼을 클릭
2. 체크인 확인 후 감정/건강 기록 카드 표시 (선택사항)
3. 감정 선택: 이모지 5단계 (매우좋음/좋음/보통/나쁨/매우나쁨) → 점수 5/4/3/2/1
4. 한줄 메모 입력 (선택, 최대 200자)
5. "건강 일지도 작성하기" 버튼으로 상세 기록 페이지 이동 (선택)
6. 저장 → 체크인 이벤트와 함께 health-journal-events Kafka 토픽 발행
```

#### 1-2. 건강 일지 상세 작성
```
1. 사용자가 건강 일지 페이지로 이동
2. 오늘의 건강 일지 항목 입력:
   a. 수면: 수면 시간 (시간), 수면 질 (1~5점)
   b. 식사: 식사 횟수 (0~5), 식사 질 (1~5점)
   c. 운동: 운동 여부, 종류, 시간 (분)
   d. 복약: 복약 여부, 특이사항
   e. 통증/불편: 부위, 강도 (1~5)
   f. 자유 메모: 최대 500자
3. 저장 → 건강 일지 데이터 저장
4. 건강 종합 점수 자동 계산 (수면 + 식사 + 운동 + 감정 가중 평균)
```

#### 1-3. 추세 분석 및 시각화
```
1. 사용자가 "내 건강 추세" 페이지로 이동
2. 기간 선택: 1주 / 2주 / 1개월 / 3개월
3. 다음 차트 표시:
   a. 감정 점수 추이 (라인 차트)
   b. 건강 종합 점수 추이 (라인 차트)
   c. 수면 시간 추이 (바 차트)
   d. 식사 횟수 추이 (바 차트)
   e. 운동 빈도 (캘린더 히트맵)
4. 기간 비교: 이번 주 vs 지난 주 요약 카드
5. 인사이트 메시지: "이번 주 감정 점수가 지난 주 대비 15% 상승했습니다" 등
```

#### 1-4. 건강 악화 감지 및 알림
```
1. 배치 작업이 매일 분석 (FEAT-002 이상 감지와 통합 또는 별도)
2. 다음 조건 감지:
   a. 감정 점수 3일 연속 2점 이하 → MOOD_DECLINING (MEDIUM)
   b. 감정 점수 7일 평균 2.5 이하 → MOOD_LOW (HIGH)
   c. 수면 시간 3일 연속 4시간 이하 → SLEEP_DEPRIVED (MEDIUM)
   d. 식사 횟수 3일 연속 1회 이하 → EATING_DISORDER_RISK (HIGH)
   e. 건강 종합 점수 7일 연속 하락 → HEALTH_DECLINING (HIGH)
   f. 통증 3일 연속 강도 4 이상 → CHRONIC_PAIN (MEDIUM)
3. 감지 시 health-alert-events Kafka 토픽 발행
4. 관리자 대시보드에 건강 악화 알림 표시
5. (선택) 사용자에게 격려 메시지 또는 도움 안내 발송
```

#### 1-5. 커뮤니티 건강 인사이트 (익명 통계)
```
1. 사용자가 "커뮤니티 인사이트" 탭 클릭
2. 동일 연령대 (10세 단위) 사용자들의 익명 통계 표시:
   - 평균 감정 점수
   - 평균 수면 시간
   - 평균 운동 빈도
3. 내 점수와 평균 비교 표시 (상위 몇 %인지)
4. 개인 식별 정보는 일절 노출하지 않음
```

### 2. 예외 흐름 (Edge Cases)

#### 2-1. 감정/건강 기록 건너뛰기
```
- 조건: 사용자가 체크인은 하되 감정/건강 기록을 원하지 않음
- 처리: "건너뛰기" 버튼으로 체크인만 완료, 건강 데이터는 null
- 원칙: 건강 기록은 항상 선택사항, 강제하지 않음
```

#### 2-2. 하루에 여러 번 건강 일지 수정
```
- 조건: 같은 날 건강 일지를 여러 번 수정
- 처리: 최신 값으로 업데이트 (upsert), 수정 이력은 MongoDB에 별도 보관
- 제한: 하루 1건의 건강 일지만 유지 (날짜 기준)
```

#### 2-3. 데이터 부족 시 추세 분석
```
- 조건: 건강 일지 기록이 3일 미만
- 처리: "추세를 확인하려면 최소 3일 이상의 기록이 필요합니다" 안내
- 차트 대신 기록 독려 메시지 표시
```

#### 2-4. 극단적 감정 기록
```
- 조건: 감정 점수 1점 (매우 나쁨) 기록
- 처리: 즉시 알림은 발생하지 않으나, 3일 연속 1~2점 시 감지 트리거
- 추가: 감정이 안 좋을 때 도움 받을 수 있는 리소스 링크 표시
  (정신건강 위기상담 전화 1577-0199, 자살예방 상담 전화 1393)
```

#### 2-5. 건강 데이터 삭제 요청
```
- 조건: 사용자가 자신의 건강 데이터 삭제를 요청
- 처리: 개인정보보호법에 따라 30일 이내 삭제 처리
- 삭제 범위: 해당 사용자의 모든 건강 일지 + 감정 기록
- 익명 통계 데이터는 이미 집계되어 있으므로 개별 삭제 불가 (익명화 완료)
```

### 3. 비즈니스 규칙

| 규칙 ID | 규칙 | 설명 |
|---------|------|------|
| BR-001 | 건강 기록은 선택사항 | 체크인 필수, 건강 기록은 선택 |
| BR-002 | 하루 1건 건강 일지 | 날짜 기준 upsert, 최신 값 유지 |
| BR-003 | 감정 점수 5단계 | 1(매우나쁨) ~ 5(매우좋음) |
| BR-004 | 건강 종합 점수 계산 | (감정*0.3 + 수면질*0.25 + 식사질*0.25 + 운동점수*0.2) * 20 = 0~100점 |
| BR-005 | 추세 분석 최소 데이터 | 최소 3일 이상 기록 필요 |
| BR-006 | 건강 악화 감지 기준 | 감정 3일 연속 2이하, 수면 3일 연속 4시간 이하 등 |
| BR-007 | 커뮤니티 인사이트 익명화 | 개인 식별 불가, 10세 단위 연령대 집계 |
| BR-008 | 건강 데이터 보관 기간 | 1년, 이후 자동 익명화 후 삭제 |
| BR-009 | 운동 점수 계산 | 운동 미수행: 1점, 30분 미만: 3점, 30분 이상: 5점 |
| BR-010 | 위기 리소스 표시 | 감정 점수 1~2점 기록 시 정신건강 상담 전화번호 표시 |

---

## API 엔드포인트 명세

### POST /api/checkins (기존 API 확장)
체크인 시 감정/건강 데이터 선택 포함

기존 체크인 API에 선택적 필드를 추가한다 (하위 호환성 유지).

**Request Body (확장):**
```json
{
  "message": "오늘도 무사합니다",
  "mood": {
    "score": 4,
    "note": "날씨가 좋아서 기분이 좋다"
  }
}
```

`mood` 필드는 선택사항. 기존 요청 형식도 정상 작동한다.

**Response 200 (확장):**
```json
{
  "status": "OK",
  "message": "체크인 완료",
  "data": {
    "checkinId": 100,
    "checkedInAt": "2026-03-14T09:15:00",
    "nextCheckinDue": "2026-03-15T09:00:00",
    "moodRecorded": true
  }
}
```

---

### POST /api/health-journals
건강 일지 작성/수정 (당일 기준 upsert)

| 항목 | 내용 |
|------|------|
| **인증** | JWT 필요 (ROLE_USER) |
| **Content-Type** | application/json |

**Request Body:**
```json
{
  "date": "2026-03-14",
  "sleep": {
    "hours": 7.5,
    "quality": 4
  },
  "meals": {
    "count": 3,
    "quality": 4
  },
  "exercise": {
    "performed": true,
    "type": "걷기",
    "durationMinutes": 45
  },
  "medication": {
    "taken": true,
    "notes": "혈압약 복용"
  },
  "pain": {
    "hasPain": false,
    "area": null,
    "intensity": null
  },
  "memo": "오늘은 전반적으로 컨디션이 좋았다"
}
```

**Response 200 (업데이트):**
```json
{
  "status": "OK",
  "message": "건강 일지가 저장되었습니다",
  "data": {
    "journalId": 50,
    "date": "2026-03-14",
    "healthScore": 82,
    "isNew": false
  }
}
```

**Response 201 (신규 생성):**
```json
{
  "status": "CREATED",
  "message": "건강 일지가 생성되었습니다",
  "data": {
    "journalId": 50,
    "date": "2026-03-14",
    "healthScore": 82,
    "isNew": true
  }
}
```

**Response 400:**
```json
{
  "status": "BAD_REQUEST",
  "message": "수면 질 점수는 1~5 사이여야 합니다"
}
```

---

### GET /api/health-journals
건강 일지 목록 조회

| 항목 | 내용 |
|------|------|
| **인증** | JWT 필요 (ROLE_USER) |
| **Query Parameters** | `startDate`, `endDate` (필수, yyyy-MM-dd 형식) |

**Response 200:**
```json
{
  "status": "OK",
  "message": "건강 일지 조회 성공",
  "data": [
    {
      "journalId": 50,
      "date": "2026-03-14",
      "moodScore": 4,
      "healthScore": 82,
      "sleepHours": 7.5,
      "mealCount": 3,
      "exercisePerformed": true,
      "hasPain": false
    },
    {
      "journalId": 49,
      "date": "2026-03-13",
      "moodScore": 3,
      "healthScore": 65,
      "sleepHours": 5.0,
      "mealCount": 2,
      "exercisePerformed": false,
      "hasPain": true
    }
  ]
}
```

---

### GET /api/health-journals/{date}
특정 날짜 건강 일지 상세 조회

| 항목 | 내용 |
|------|------|
| **인증** | JWT 필요 (ROLE_USER) |
| **Path Parameter** | `date`: yyyy-MM-dd 형식 |

**Response 200:**
```json
{
  "status": "OK",
  "message": "건강 일지 상세 조회 성공",
  "data": {
    "journalId": 50,
    "date": "2026-03-14",
    "mood": {
      "score": 4,
      "note": "날씨가 좋아서 기분이 좋다"
    },
    "sleep": {
      "hours": 7.5,
      "quality": 4
    },
    "meals": {
      "count": 3,
      "quality": 4
    },
    "exercise": {
      "performed": true,
      "type": "걷기",
      "durationMinutes": 45
    },
    "medication": {
      "taken": true,
      "notes": "혈압약 복용"
    },
    "pain": {
      "hasPain": false,
      "area": null,
      "intensity": null
    },
    "memo": "오늘은 전반적으로 컨디션이 좋았다",
    "healthScore": 82,
    "createdAt": "2026-03-14T09:20:00",
    "updatedAt": "2026-03-14T09:20:00"
  }
}
```

**Response 404:**
```json
{
  "status": "NOT_FOUND",
  "message": "해당 날짜의 건강 일지가 없습니다"
}
```

---

### GET /api/health-journals/trends
건강 추세 분석 데이터 조회

| 항목 | 내용 |
|------|------|
| **인증** | JWT 필요 (ROLE_USER) |
| **Query Parameters** | `period`: WEEK/TWO_WEEKS/MONTH/THREE_MONTHS |

**Response 200:**
```json
{
  "status": "OK",
  "message": "건강 추세 분석 조회 성공",
  "data": {
    "period": "MONTH",
    "startDate": "2026-02-14",
    "endDate": "2026-03-14",
    "totalEntries": 25,
    "moodTrend": {
      "data": [
        {"date": "2026-02-14", "score": 3},
        {"date": "2026-02-15", "score": 4},
        {"date": "2026-02-16", "score": null}
      ],
      "average": 3.6,
      "trend": "STABLE",
      "changePercent": 2.5
    },
    "healthScoreTrend": {
      "data": [
        {"date": "2026-02-14", "score": 70},
        {"date": "2026-02-15", "score": 75}
      ],
      "average": 72.5,
      "trend": "IMPROVING",
      "changePercent": 8.0
    },
    "sleepTrend": {
      "data": [
        {"date": "2026-02-14", "hours": 6.5},
        {"date": "2026-02-15", "hours": 7.0}
      ],
      "average": 6.8,
      "trend": "STABLE"
    },
    "mealTrend": {
      "data": [
        {"date": "2026-02-14", "count": 2},
        {"date": "2026-02-15", "count": 3}
      ],
      "average": 2.5,
      "trend": "STABLE"
    },
    "exerciseFrequency": {
      "totalDays": 12,
      "activeDays": 8,
      "rate": 0.67
    },
    "insights": [
      {
        "type": "POSITIVE",
        "message": "이번 달 건강 종합 점수가 지난 달 대비 8% 상승했습니다"
      },
      {
        "type": "SUGGESTION",
        "message": "수면 시간이 권장 시간(7시간)보다 다소 부족합니다"
      }
    ],
    "comparison": {
      "previousPeriod": {
        "moodAverage": 3.5,
        "healthScoreAverage": 67.0,
        "sleepAverage": 6.5
      },
      "currentPeriod": {
        "moodAverage": 3.6,
        "healthScoreAverage": 72.5,
        "sleepAverage": 6.8
      }
    }
  }
}
```

**Response 400:**
```json
{
  "status": "BAD_REQUEST",
  "message": "추세를 확인하려면 최소 3일 이상의 기록이 필요합니다"
}
```

---

### GET /api/health-journals/community-insights
커뮤니티 건강 인사이트 (익명 통계)

| 항목 | 내용 |
|------|------|
| **인증** | JWT 필요 (ROLE_USER) |

**Response 200:**
```json
{
  "status": "OK",
  "message": "커뮤니티 인사이트 조회 성공",
  "data": {
    "ageGroup": "40대",
    "sampleSize": 150,
    "communityAverage": {
      "moodScore": 3.4,
      "healthScore": 68,
      "sleepHours": 6.5,
      "exerciseRate": 0.45
    },
    "myAverage": {
      "moodScore": 3.6,
      "healthScore": 72.5,
      "sleepHours": 6.8,
      "exerciseRate": 0.67
    },
    "percentile": {
      "moodScore": 62,
      "healthScore": 70,
      "sleepHours": 65,
      "exerciseRate": 80
    }
  }
}
```

---

### DELETE /api/health-journals
건강 데이터 전체 삭제 요청

| 항목 | 내용 |
|------|------|
| **인증** | JWT 필요 (ROLE_USER) |

**Response 200:**
```json
{
  "status": "OK",
  "message": "건강 데이터 삭제가 요청되었습니다. 30일 이내에 처리됩니다.",
  "data": {
    "deletionRequestedAt": "2026-03-14T10:00:00",
    "estimatedDeletionDate": "2026-04-13"
  }
}
```

---

### GET /api/admin/health-alerts
관리자 건강 악화 알림 조회

| 항목 | 내용 |
|------|------|
| **인증** | Basic Auth (ROLE_ADMIN) |
| **Query Parameters** | `severity`: LOW/MEDIUM/HIGH (optional), `alertType` (optional), `page`, `size` |

**Response 200:**
```json
{
  "status": "OK",
  "message": "건강 악화 알림 조회 성공",
  "data": {
    "content": [
      {
        "alertId": "6600b1c2d3e4f5a6b7c8d9e0",
        "userId": 42,
        "userName": "홍길동",
        "severity": "HIGH",
        "alertType": "MOOD_LOW",
        "description": "최근 7일 평균 감정 점수 2.1 (기준: 2.5 이하)",
        "detectedAt": "2026-03-14T02:30:00",
        "recentMoodScores": [2, 1, 3, 2, 2, 2, 3],
        "acknowledged": false
      }
    ],
    "totalElements": 8,
    "totalPages": 1
  }
}
```

---

### POST /api/admin/health-alerts/{alertId}/acknowledge
건강 악화 알림 확인 처리

| 항목 | 내용 |
|------|------|
| **인증** | Basic Auth (ROLE_ADMIN) |
| **Content-Type** | application/json |

**Request Body:**
```json
{
  "actionTaken": "사용자에게 전화 연락, 상담 기관 안내 완료"
}
```

**Response 200:**
```json
{
  "status": "OK",
  "message": "건강 악화 알림이 확인 처리되었습니다"
}
```

---

## 데이터 모델 변경

### 신규 테이블: health_journals (PostgreSQL)
```sql
CREATE TABLE health_journals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    journal_date DATE NOT NULL,
    mood_score INT,                      -- 1~5
    mood_note VARCHAR(200),
    sleep_hours DECIMAL(3, 1),           -- 0.0~24.0
    sleep_quality INT,                   -- 1~5
    meal_count INT,                      -- 0~5
    meal_quality INT,                    -- 1~5
    exercise_performed BOOLEAN DEFAULT false,
    exercise_type VARCHAR(50),
    exercise_duration_minutes INT,
    medication_taken BOOLEAN DEFAULT false,
    medication_notes VARCHAR(200),
    pain_has_pain BOOLEAN DEFAULT false,
    pain_area VARCHAR(50),
    pain_intensity INT,                  -- 1~5
    memo VARCHAR(500),
    health_score INT,                    -- 0~100, 자동 계산
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE (user_id, journal_date)
);

CREATE INDEX idx_health_journals_user_date ON health_journals (user_id, journal_date DESC);
CREATE INDEX idx_health_journals_mood ON health_journals (user_id, mood_score, journal_date);
```

### 기존 테이블 확장: checkins (PostgreSQL)
```sql
ALTER TABLE checkins ADD COLUMN mood_score INT;       -- 1~5, 선택
ALTER TABLE checkins ADD COLUMN mood_note VARCHAR(200); -- 선택
```

### 신규 컬렉션: health_alerts (MongoDB)
```json
{
  "_id": "ObjectId",
  "userId": 42,
  "severity": "HIGH",
  "alertType": "MOOD_LOW",
  "description": "최근 7일 평균 감정 점수 2.1",
  "metrics": {
    "threshold": 2.5,
    "actualValue": 2.1,
    "dataPoints": [2, 1, 3, 2, 2, 2, 3],
    "period": {
      "from": "2026-03-07",
      "to": "2026-03-13"
    }
  },
  "acknowledged": false,
  "acknowledgedBy": null,
  "acknowledgedAt": null,
  "actionTaken": null,
  "detectedAt": "2026-03-14T02:30:00",
  "createdAt": "2026-03-14T02:30:00"
}
```

### 신규 컬렉션: health_journal_history (MongoDB)
건강 일지 수정 이력 보관 (감사 로그)
```json
{
  "_id": "ObjectId",
  "journalId": 50,
  "userId": 42,
  "date": "2026-03-14",
  "previousData": {
    "moodScore": 3,
    "sleepHours": 6.0,
    "healthScore": 65
  },
  "updatedData": {
    "moodScore": 4,
    "sleepHours": 7.5,
    "healthScore": 82
  },
  "updatedAt": "2026-03-14T15:00:00"
}
```

### 신규 컬렉션: community_health_stats (MongoDB)
커뮤니티 통계 집계 (일별 배치)
```json
{
  "_id": "ObjectId",
  "ageGroup": "40",
  "date": "2026-03-14",
  "sampleSize": 150,
  "averageMoodScore": 3.4,
  "averageHealthScore": 68,
  "averageSleepHours": 6.5,
  "exerciseRate": 0.45,
  "createdAt": "2026-03-14T03:00:00"
}
```

---

## Kafka 토픽

### 신규 토픽: health-journal-events
```json
{
  "eventType": "MOOD_RECORDED",
  "userId": 42,
  "date": "2026-03-14",
  "moodScore": 4,
  "healthScore": 82,
  "timestamp": "2026-03-14T09:20:00"
}
```

### 신규 토픽: health-alert-events
```json
{
  "eventType": "HEALTH_ALERT_CREATED",
  "userId": 42,
  "alertId": "6600b1c2d3e4f5a6b7c8d9e0",
  "alertType": "MOOD_LOW",
  "severity": "HIGH",
  "timestamp": "2026-03-14T02:30:00"
}
```

### 기존 토픽 연동: checkin-events
체크인 이벤트에 mood 데이터를 포함하여 발행 (기존 필드에 추가, 하위 호환)

---

## 수용 기준 (Acceptance Criteria)

### 감정 기록 (체크인 연동)
- [ ] 체크인 시 감정 점수 (1~5)를 선택적으로 기록할 수 있다
- [ ] 감정에 대한 한줄 메모를 선택적으로 작성할 수 있다
- [ ] 감정 기록 없이도 체크인이 정상 완료된다 (기존 호환성)
- [ ] 감정 점수 1~2 기록 시 위기 상담 전화번호가 표시된다

### 건강 일지
- [ ] 수면, 식사, 운동, 복약, 통증 정보를 기록할 수 있다
- [ ] 같은 날짜에 대해 건강 일지를 수정할 수 있다 (upsert)
- [ ] 하루 1건의 건강 일지만 유지된다
- [ ] 건강 종합 점수가 자동으로 계산된다 (0~100점)
- [ ] 날짜 범위로 건강 일지 목록을 조회할 수 있다
- [ ] 특정 날짜의 건강 일지 상세를 조회할 수 있다

### 추세 분석
- [ ] 1주/2주/1개월/3개월 기간별 추세 차트가 표시된다
- [ ] 감정 점수 추이 라인 차트가 표시된다
- [ ] 건강 종합 점수 추이 라인 차트가 표시된다
- [ ] 수면/식사/운동 추이가 표시된다
- [ ] 이전 기간과의 비교 요약이 표시된다
- [ ] 인사이트 메시지가 자동 생성된다
- [ ] 3일 미만 데이터 시 추세 조회가 제한된다

### 건강 악화 감지
- [ ] 감정 3일 연속 2점 이하 시 MEDIUM 알림이 생성된다
- [ ] 감정 7일 평균 2.5 이하 시 HIGH 알림이 생성된다
- [ ] 수면 3일 연속 4시간 이하 시 MEDIUM 알림이 생성된다
- [ ] 식사 3일 연속 1회 이하 시 HIGH 알림이 생성된다
- [ ] 관리자가 건강 악화 알림 목록을 조회할 수 있다
- [ ] 관리자가 건강 악화 알림을 확인 처리할 수 있다

### 커뮤니티 인사이트
- [ ] 동일 연령대의 익명 통계가 표시된다
- [ ] 내 점수와 평균을 비교할 수 있다
- [ ] 백분위 순위가 표시된다
- [ ] 개인 식별 정보가 노출되지 않는다

### 데이터 관리
- [ ] 사용자가 건강 데이터 전체 삭제를 요청할 수 있다
- [ ] 수정 이력이 MongoDB에 보관된다

### 기존 기능 호환성
- [ ] 기존 체크인 API가 mood 필드 없이도 정상 작동한다
- [ ] 기존 체크인 이벤트 소비자가 정상 작동한다 (mood 필드 무시)

---

## 우선순위
**P1 (중요)** - 체크인 데이터 풍부화를 통한 이상 감지 정확도 향상 및 사용자 참여도 증가. FEAT-002와 병행 또는 후속 개발.

---

## 구현 순서 (권장)

| 단계 | 작업 | 담당 | 예상 기간 |
|------|------|------|----------|
| 1 | DB 스키마 생성 (health_journals 테이블, checkins 확장, MongoDB 컬렉션) | Backend | 1일 |
| 2 | 건강 일지 엔티티/리포지토리/서비스 구현 | Backend | 2일 |
| 3 | 체크인 API 확장 (mood 필드 추가) | Backend | 1일 |
| 4 | 건강 일지 CRUD API 구현 | Backend | 2일 |
| 5 | 추세 분석 서비스 구현 (통계 계산 + 인사이트 생성) | Backend | 3일 |
| 6 | 건강 악화 감지 배치 작업 구현 | Backend | 2일 |
| 7 | 커뮤니티 통계 집계 배치 작업 구현 | Backend | 1일 |
| 8 | Kafka 토픽 및 이벤트 발행/소비 구현 | Backend | 1일 |
| 9 | 관리자 건강 알림 API 구현 | Backend | 1일 |
| 10 | 체크인 UI 감정 선택 카드 | Frontend | 1일 |
| 11 | 건강 일지 작성 페이지 | Frontend | 2일 |
| 12 | 추세 분석 차트 페이지 (Chart.js / Recharts) | Frontend | 3일 |
| 13 | 커뮤니티 인사이트 UI | Frontend | 1일 |
| 14 | 관리자 건강 알림 대시보드 UI | Frontend | 1일 |
| 15 | 통합 테스트 및 QA | QA | 3일 |

---

## 디자인팀 인터페이스 (Design Team)

### 필요한 UI 변경사항

#### 1. 체크인 감정 선택 카드
- 체크인 완료 후 표시되는 감정 카드
- 5단계 이모지 선택: 매우좋음/좋음/보통/나쁨/매우나쁨
- 한줄 메모 입력 필드
- "건강 일지도 작성하기" 링크 버튼
- "건너뛰기" 버튼
- 감정 1~2점 시 위기 상담 안내 배너

#### 2. 건강 일지 작성 페이지 (`/health-journal`)
- 날짜 선택 (기본 오늘)
- 수면: 시간 슬라이더 + 질 별점 (별 5개)
- 식사: 횟수 숫자 입력 + 질 별점
- 운동: 토글 + 종류 입력 + 시간 슬라이더
- 복약: 토글 + 메모
- 통증: 토글 + 부위 선택 (인체 그림 또는 드롭다운) + 강도 슬라이더
- 자유 메모 텍스트 입력
- 건강 종합 점수 실시간 표시 (게이지 차트)

#### 3. 건강 추세 페이지 (`/health-journal/trends`)
- 기간 선택 탭 (1주 / 2주 / 1개월 / 3개월)
- 감정 추이 라인 차트 (색상: 점수별 그라데이션)
- 건강 종합 점수 추이 라인 차트
- 수면/식사 바 차트
- 운동 캘린더 히트맵
- 이전 기간 비교 카드 (상승/하락 화살표)
- 인사이트 메시지 카드

#### 4. 커뮤니티 인사이트 탭 (추세 페이지 내)
- 내 점수 vs 평균 비교 바 차트
- 백분위 표시 (원형 게이지)
- 연령대 표시

#### 5. 관리자 건강 알림 페이지
- severity별 색상 (LOW: 파랑, MEDIUM: 주황, HIGH: 빨강)
- 알림 카드: 사용자명, 유형, 설명, 최근 감정 점수 미니 차트
- 확인 처리 모달 (조치 사항 입력)

### 디자인팀 산출물 요청
- 감정 이모지 아이콘 세트 (5종)
- 건강 일지 폼 레이아웃
- 차트 색상 팔레트 및 스타일 가이드
- 인사이트 카드 디자인
- 위기 상담 안내 배너 디자인

---

## 개발팀 인터페이스 (Development Team)

### Backend 변경사항

#### 1. 신규 클래스 (api-service)
| 패키지 | 클래스 | 역할 |
|--------|--------|------|
| `domain` | `HealthJournal` | 건강 일지 엔티티 |
| `repository` | `HealthJournalRepository` | 건강 일지 JPA Repository |
| `service` | `HealthJournalService` | 건강 일지 CRUD + 점수 계산 |
| `service` | `HealthTrendService` | 추세 분석 + 인사이트 생성 |
| `service` | `HealthScoreCalculator` | 건강 종합 점수 계산 로직 |
| `controller` | `HealthJournalController` | 건강 일지 API |
| `dto/health` | `HealthJournalRequest` | 건강 일지 요청 DTO |
| `dto/health` | `HealthJournalResponse` | 건강 일지 응답 DTO |
| `dto/health` | `HealthTrendResponse` | 추세 분석 응답 DTO |
| `dto/health` | `CommunityInsightResponse` | 커뮤니티 인사이트 응답 DTO |
| `dto/checkin` | `MoodRequest` (내부) | 체크인 감정 데이터 |

#### 2. 기존 코드 변경 (api-service)
| 파일 | 변경 내용 |
|------|----------|
| `Checkin.java` | `moodScore`, `moodNote` 필드 추가 |
| `CheckinRequest.java` | `mood` 선택 필드 추가 |
| `CheckinResponse.java` | `moodRecorded` 필드 추가 |
| `CheckinService.java` | 체크인 시 mood 저장 로직 추가 |
| `CheckinEventPublisher` | 이벤트에 mood 데이터 포함 |

#### 3. 신규 클래스 (admin-service)
| 패키지 | 클래스 | 역할 |
|--------|--------|------|
| `controller` | `HealthAlertAdminController` | 건강 알림 관리 API |
| `service` | `HealthAlertAdminService` | 건강 알림 조회/확인 로직 |

#### 4. 신규 클래스 (event-service)
| 패키지 | 클래스 | 역할 |
|--------|--------|------|
| `consumer` | `HealthJournalEventConsumer` | 건강 일지 이벤트 소비 + MongoDB 로깅 |
| `consumer` | `HealthAlertEventConsumer` | 건강 알림 이벤트 소비 + MongoDB 로깅 |
| `service` | `HealthDeteriorationDetector` | 건강 악화 감지 배치 로직 |
| `service` | `CommunityStatsAggregator` | 커뮤니티 통계 집계 배치 |
| `domain` | `HealthAlert` | 건강 알림 MongoDB Document |
| `domain` | `HealthJournalHistory` | 건강 일지 수정 이력 MongoDB Document |
| `domain` | `CommunityHealthStat` | 커뮤니티 통계 MongoDB Document |
| `repository` | `HealthAlertRepository` | MongoDB Repository |
| `repository` | `CommunityHealthStatRepository` | MongoDB Repository |

#### 5. 신규 클래스 (common 모듈)
| 패키지 | 클래스 | 역할 |
|--------|--------|------|
| `event` | `HealthJournalEvent` | 건강 일지 Kafka 이벤트 DTO |
| `event` | `HealthAlertEvent` | 건강 알림 Kafka 이벤트 DTO |

### Frontend 변경사항

#### 1. 신규 페이지/컴포넌트
| 경로/이름 | 유형 | 역할 |
|-----------|------|------|
| `health-journal/page.tsx` | Page | 건강 일지 작성 |
| `health-journal/trends/page.tsx` | Page | 추세 분석 + 커뮤니티 인사이트 |
| `MoodSelector.tsx` | Component | 감정 이모지 5단계 선택 |
| `MoodCard.tsx` | Component | 체크인 후 감정 기록 카드 |
| `HealthJournalForm.tsx` | Component | 건강 일지 입력 폼 |
| `HealthScoreGauge.tsx` | Component | 건강 종합 점수 게이지 |
| `MoodTrendChart.tsx` | Component | 감정 추이 라인 차트 |
| `HealthScoreTrendChart.tsx` | Component | 건강 점수 추이 라인 차트 |
| `SleepMealChart.tsx` | Component | 수면/식사 바 차트 |
| `ExerciseHeatmap.tsx` | Component | 운동 캘린더 히트맵 |
| `InsightCard.tsx` | Component | 인사이트 메시지 카드 |
| `CommunityInsight.tsx` | Component | 커뮤니티 비교 UI |
| `CrisisBanner.tsx` | Component | 위기 상담 안내 배너 |
| `admin/health-alerts/page.tsx` | Page | 관리자 건강 알림 |

#### 2. 기존 코드 변경
| 파일 | 변경 내용 |
|------|----------|
| `checkin 관련 컴포넌트` | 체크인 후 MoodCard 표시 |
| `dashboard/page.tsx` | 오늘의 감정/건강 점수 요약 카드 추가 |
| `admin/page.tsx` | 건강 알림 요약 카드 추가 |
| `src/types/index.ts` | HealthJournal, MoodScore, HealthTrend 등 타입 추가 |
| `src/lib/api.ts` | 건강 일지/추세/커뮤니티 API 함수 추가 |
| 네비게이션 | "건강 일지" 메뉴 추가 |

#### 3. 차트 라이브러리
- 권장: Recharts (React 기반, 커스터마이징 용이)
- 대안: Chart.js + react-chartjs-2
- 캘린더 히트맵: react-calendar-heatmap 또는 커스텀 구현

---

## 참고 자료
- 현재 체크인 코드: `backend/api-service/` 하위 Checkin 관련 Controller/Service/Entity
- 현재 이벤트 코드: `backend/event-service/` 하위 Kafka Consumer
- FEAT-002 이상 감지 시스템과의 연동 필요
- 정신건강 위기상담 전화: 1577-0199
- 자살예방 상담 전화: 1393
- Recharts: https://recharts.org/
