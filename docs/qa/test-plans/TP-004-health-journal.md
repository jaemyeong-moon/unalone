# TP-004: 건강 일지 & 감정 추적 테스트 계획

- **기능 명세**: FEAT-004 (Health Journal & Mood Tracking)
- **작성일**: 2026-03-14
- **우선순위**: P1 (중요) - 체크인 데이터 풍부화 및 이상 감지 정확도 향상
- **테스트 범위**: 건강 일지 CRUD, 건강 점수 계산, 감정 기록, 추세 분석, 건강 악화 감지, 커뮤니티 인사이트, API 계약

---

## 1. 건강 일지 CRUD

### TC-004-001: 건강 일지 신규 생성
- **우선순위**: P0
- **유형**: Integration
- **사전 조건**: 사용자 로그인 완료 (JWT), 오늘 날짜의 건강 일지 미존재
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `POST /api/health-journals` 호출, 전체 필드 입력 (수면, 식사, 운동, 복약, 통증, 메모) | 201 CREATED, `isNew: true`, `healthScore` 계산됨 | |
| 2 | `GET /api/health-journals/2026-03-14` 조회 | 저장된 데이터와 일치 | |

- **테스트 데이터**:
```json
{
  "date": "2026-03-14",
  "sleep": {"hours": 7.5, "quality": 4},
  "meals": {"count": 3, "quality": 4},
  "exercise": {"performed": true, "type": "걷기", "durationMinutes": 45},
  "medication": {"taken": true, "notes": "혈압약 복용"},
  "pain": {"hasPain": false},
  "memo": "컨디션 좋음"
}
```

### TC-004-002: 건강 일지 수정 (같은 날 upsert)
- **우선순위**: P0
- **유형**: Integration
- **사전 조건**: 오늘 날짜 건강 일지가 이미 존재
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `POST /api/health-journals` 동일 날짜로 수정된 데이터 전송 | 200 OK, `isNew: false` | |
| 2 | 수정된 `healthScore` 확인 | 새로운 데이터 기반으로 재계산됨 | |
| 3 | `GET /api/health-journals/2026-03-14` 조회 | 최신 값으로 반영됨, `updatedAt` 갱신됨 | |
| 4 | MongoDB health_journal_history 확인 | 수정 이전 데이터가 이력으로 보관됨 | |

### TC-004-003: 건강 일지 목록 조회 (날짜 범위)
- **우선순위**: P0
- **유형**: Integration
- **사전 조건**: 최근 7일간 건강 일지 5건 존재
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `GET /api/health-journals?startDate=2026-03-08&endDate=2026-03-14` | 200 OK, 5건 반환 (기록 없는 날짜는 제외) | |
| 2 | 각 항목에 요약 필드 포함 확인 | `moodScore`, `healthScore`, `sleepHours`, `mealCount`, `exercisePerformed`, `hasPain` | |
| 3 | 날짜 역순 정렬 확인 | 최신 날짜가 먼저 | |

### TC-004-004: 특정 날짜 건강 일지 상세 조회
- **우선순위**: P0
- **유형**: Integration
- **사전 조건**: 해당 날짜 건강 일지 존재
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `GET /api/health-journals/2026-03-14` | 200 OK, 모든 상세 필드 반환 (mood, sleep, meals, exercise, medication, pain, memo, healthScore) | |
| 2 | 존재하지 않는 날짜 조회 `GET /api/health-journals/2020-01-01` | 404 NOT_FOUND, "해당 날짜의 건강 일지가 없습니다" | |

### TC-004-005: 건강 일지 날짜 중복 방지 (하루 1건)
- **우선순위**: P0
- **유형**: Integration
- **사전 조건**: 오늘 건강 일지 이미 존재
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | 동일 날짜로 `POST /api/health-journals` 재호출 | 200 OK (upsert), 기존 데이터 업데이트 (신규 생성 아님) | |
| 2 | DB 확인 | 동일 `user_id + journal_date` 조합에 1건만 존재 | |

### TC-004-006: 건강 데이터 전체 삭제 요청
- **우선순위**: P1
- **유형**: Integration
- **사전 조건**: 사용자의 건강 일지 데이터 다수 존재
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `DELETE /api/health-journals` 호출 | 200 OK, `deletionRequestedAt` 및 `estimatedDeletionDate` (30일 이내) 반환 | |
| 2 | 삭제 요청 후 데이터 조회 시도 | 즉시 삭제되지 않고 조회 가능 (30일 유예) | |

---

## 2. 건강 점수 계산 정확성

### TC-004-007: 건강 종합 점수 계산 - 모든 항목 입력
- **우선순위**: P0
- **유형**: Unit
- **사전 조건**: N/A
- **공식**: `(감정*0.3 + 수면질*0.25 + 식사질*0.25 + 운동점수*0.2) * 20`
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | 감정: 5, 수면질: 5, 식사질: 5, 운동: 30분 이상 (5점) | healthScore = (5*0.3 + 5*0.25 + 5*0.25 + 5*0.2) * 20 = 100 | |
| 2 | 감정: 1, 수면질: 1, 식사질: 1, 운동: 미수행 (1점) | healthScore = (1*0.3 + 1*0.25 + 1*0.25 + 1*0.2) * 20 = 20 | |
| 3 | 감정: 3, 수면질: 4, 식사질: 3, 운동: 30분 미만 (3점) | healthScore = (3*0.3 + 4*0.25 + 3*0.25 + 3*0.2) * 20 = (0.9+1.0+0.75+0.6)*20 = 65 | |
| 4 | 감정: 4, 수면질: 4, 식사질: 4, 운동: 45분 (5점) | healthScore = (4*0.3 + 4*0.25 + 4*0.25 + 5*0.2) * 20 = (1.2+1.0+1.0+1.0)*20 = 84 | |

### TC-004-008: 운동 점수 계산 규칙
- **우선순위**: P0
- **유형**: Unit
- **사전 조건**: N/A
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `exercise.performed: false` | 운동 점수 = 1 | |
| 2 | `exercise.performed: true`, `durationMinutes: 15` (30분 미만) | 운동 점수 = 3 | |
| 3 | `exercise.performed: true`, `durationMinutes: 30` (정확히 30분) | 운동 점수 = 5 | |
| 4 | `exercise.performed: true`, `durationMinutes: 60` (30분 이상) | 운동 점수 = 5 | |

### TC-004-009: 건강 점수 계산 - 부분 입력 (감정만 입력)
- **우선순위**: P1
- **유형**: Unit
- **사전 조건**: 수면/식사/운동 미입력, 감정만 입력
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | 감정: 4, 나머지 null | healthScore 계산 시 null 항목 제외하고 가용 데이터로 계산 또는 감정만 반영된 부분 점수 반환 | |
| 2 | 응답에 healthScore 포함 | null이 아닌 유효한 점수 반환 | |

---

## 3. 감정(Mood) 기록 - 체크인 연동

### TC-004-010: 체크인 시 감정 점수 기록
- **우선순위**: P0
- **유형**: Integration
- **사전 조건**: 사용자 로그인, 체크인 가능 상태
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `POST /api/checkins` 호출, body에 `mood: {"score": 4, "note": "좋은 하루"}` 포함 | 200 OK, `moodRecorded: true` | |
| 2 | checkins 테이블 확인 | `mood_score: 4`, `mood_note: "좋은 하루"` 저장됨 | |
| 3 | health-journal-events Kafka 토픽 확인 | `eventType: MOOD_RECORDED`, `moodScore: 4` 이벤트 발행됨 | |

### TC-004-011: 체크인 시 감정 기록 건너뛰기 (하위 호환성)
- **우선순위**: P0
- **유형**: Integration
- **사전 조건**: 사용자 로그인
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `POST /api/checkins` 호출, `mood` 필드 없이 기존 형식만 전송: `{"message": "무사합니다"}` | 200 OK, 체크인 정상 완료 | |
| 2 | checkins 테이블 확인 | `mood_score: null`, `mood_note: null` | |
| 3 | 응답 확인 | `moodRecorded: false` | |

### TC-004-012: 감정 점수 1~2점 기록 시 위기 리소스 표시
- **우선순위**: P0
- **유형**: Integration
- **사전 조건**: 사용자 로그인
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `POST /api/checkins` 호출, `mood.score: 1` | 200 OK, 체크인 완료 | |
| 2 | 프론트엔드에서 위기 상담 안내 표시 | 정신건강 위기상담 전화 1577-0199, 자살예방 상담 전화 1393 표시 | |
| 3 | `mood.score: 2` 로 체크인 | 동일하게 위기 리소스 표시 | |
| 4 | `mood.score: 3` 로 체크인 | 위기 리소스 미표시 | |

---

## 4. 추세 분석

### TC-004-013: 주간 추세 조회 (WEEK)
- **우선순위**: P0
- **유형**: Integration
- **사전 조건**: 최근 7일간 건강 일지 5건 이상 존재
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `GET /api/health-journals/trends?period=WEEK` | 200 OK | |
| 2 | `moodTrend.data` 확인 | 최근 7일간 일별 감정 점수 (null 가능) | |
| 3 | `healthScoreTrend.average` 확인 | 기록 있는 날의 평균값 | |
| 4 | `sleepTrend`, `mealTrend` 확인 | 수면/식사 추세 데이터 포함 | |
| 5 | `exerciseFrequency` 확인 | totalDays, activeDays, rate 정확 | |
| 6 | `comparison` 확인 | 이번 주 vs 지난 주 비교 데이터 | |
| 7 | `insights` 확인 | 최소 1개 이상의 인사이트 메시지 | |

### TC-004-014: 월간 추세 조회 (MONTH)
- **우선순위**: P1
- **유형**: Integration
- **사전 조건**: 최근 30일간 건강 일지 15건 이상 존재
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `GET /api/health-journals/trends?period=MONTH` | 200 OK, 30일 범위 데이터 반환 | |
| 2 | `startDate`, `endDate` 확인 | 정확히 30일 범위 | |
| 3 | `trend` 필드 확인 | IMPROVING / STABLE / DECLINING 중 하나 | |
| 4 | `changePercent` 확인 | 이전 기간 대비 변화율이 수치로 표시 | |

### TC-004-015: 3개월 추세 조회 (THREE_MONTHS)
- **우선순위**: P2
- **유형**: Integration
- **사전 조건**: 최근 90일간 건강 일지 존재
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `GET /api/health-journals/trends?period=THREE_MONTHS` | 200 OK, 90일 범위 데이터 반환 | |

### TC-004-016: 추세 조회 - 데이터 부족 (3일 미만)
- **우선순위**: P0
- **유형**: Integration
- **사전 조건**: 건강 일지 2건만 존재 (신규 사용자)
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `GET /api/health-journals/trends?period=WEEK` | 400 BAD_REQUEST, "추세를 확인하려면 최소 3일 이상의 기록이 필요합니다" | |

### TC-004-017: 추세 - trend 방향 계산 정확성
- **우선순위**: P1
- **유형**: Unit
- **사전 조건**: 다양한 추세 데이터 세트
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | 최근 기간 평균 > 이전 기간 평균 (유의미한 차이) | `trend: IMPROVING` | |
| 2 | 최근 기간 평균 < 이전 기간 평균 (유의미한 차이) | `trend: DECLINING` | |
| 3 | 최근 기간 평균 ≈ 이전 기간 평균 | `trend: STABLE` | |

---

## 5. 건강 악화 감지

### TC-004-018: 감정 3일 연속 2점 이하 감지 (MOOD_DECLINING)
- **우선순위**: P0
- **유형**: Integration
- **사전 조건**: 사용자가 3일 연속 감정 점수 2, 1, 2 기록
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | 배치 작업 실행 | health_alerts MongoDB에 `alertType: MOOD_DECLINING`, `severity: MEDIUM` 생성 | |
| 2 | health-alert-events Kafka 토픽 확인 | 이벤트 발행됨 | |
| 3 | `GET /api/admin/health-alerts` 확인 | 해당 알림 표시됨 | |

### TC-004-019: 감정 7일 평균 2.5 이하 감지 (MOOD_LOW)
- **우선순위**: P0
- **유형**: Integration
- **사전 조건**: 사용자의 최근 7일 감정 평균이 2.3
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | 배치 작업 실행 | `alertType: MOOD_LOW`, `severity: HIGH` 생성 | |
| 2 | `recentMoodScores` 확인 | 최근 7일 점수 배열 포함 | |

### TC-004-020: 수면 3일 연속 4시간 이하 감지 (SLEEP_DEPRIVED)
- **우선순위**: P0
- **유형**: Integration
- **사전 조건**: 사용자가 3일 연속 수면 시간 3.5, 4.0, 3.0 기록
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | 배치 작업 실행 | `alertType: SLEEP_DEPRIVED`, `severity: MEDIUM` 생성 | |

### TC-004-021: 식사 3일 연속 1회 이하 감지 (EATING_DISORDER_RISK)
- **우선순위**: P0
- **유형**: Integration
- **사전 조건**: 사용자가 3일 연속 식사 횟수 1, 0, 1 기록
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | 배치 작업 실행 | `alertType: EATING_DISORDER_RISK`, `severity: HIGH` 생성 | |

### TC-004-022: 건강 종합 점수 7일 연속 하락 감지 (HEALTH_DECLINING)
- **우선순위**: P1
- **유형**: Integration
- **사전 조건**: 7일간 healthScore: 80, 75, 70, 65, 60, 55, 50 (연속 하락)
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | 배치 작업 실행 | `alertType: HEALTH_DECLINING`, `severity: HIGH` 생성 | |

### TC-004-023: 통증 3일 연속 강도 4 이상 감지 (CHRONIC_PAIN)
- **우선순위**: P1
- **유형**: Integration
- **사전 조건**: 사용자가 3일 연속 통증 강도 4, 5, 4 기록
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | 배치 작업 실행 | `alertType: CHRONIC_PAIN`, `severity: MEDIUM` 생성 | |

### TC-004-024: 악화 미감지 - 기준 미달
- **우선순위**: P1
- **유형**: Unit
- **사전 조건**: 감정 점수 2, 3, 2 (2일만 2점 이하, 연속 3일 아님)
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | 배치 작업 실행 | MOOD_DECLINING 알림 미생성 | |
| 2 | 감정 점수 3, 2, 3, 2 (연속 아님) | 알림 미생성 | |

---

## 6. 에지 케이스

### TC-004-025: 부분 건강 일지 (감정만 입력)
- **우선순위**: P1
- **유형**: Integration
- **사전 조건**: 사용자 로그인
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `POST /api/health-journals`, sleep/meals/exercise/pain 모두 null, mood만 입력 | 201 CREATED, 부분 데이터로 저장됨 | |
| 2 | `healthScore` 확인 | null이 아닌 유효한 부분 점수 반환 | |

### TC-004-026: 경계값 테스트 - 감정 점수
- **우선순위**: P0
- **유형**: Unit
- **사전 조건**: N/A
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `mood.score: 1` (최소) | 정상 저장 | |
| 2 | `mood.score: 5` (최대) | 정상 저장 | |
| 3 | `mood.score: 0` (범위 미달) | 400 BAD_REQUEST | |
| 4 | `mood.score: 6` (범위 초과) | 400 BAD_REQUEST | |
| 5 | `mood.score: -1` (음수) | 400 BAD_REQUEST | |

### TC-004-027: 경계값 테스트 - 통증 강도
- **우선순위**: P1
- **유형**: Unit
- **사전 조건**: N/A
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `pain.intensity: 1` (최소) | 정상 저장 | |
| 2 | `pain.intensity: 5` (최대) | 정상 저장 | |
| 3 | `pain.intensity: 0` (범위 미달) | 400 BAD_REQUEST | |
| 4 | `pain.intensity: 6` (범위 초과) | 400 BAD_REQUEST | |
| 5 | `pain.hasPain: true`, `pain.intensity: null` | 400 BAD_REQUEST (통증 있으면 강도 필수) | |

### TC-004-028: 경계값 테스트 - 수면 시간
- **우선순위**: P1
- **유형**: Unit
- **사전 조건**: N/A
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `sleep.hours: 0.0` (최소) | 정상 저장 | |
| 2 | `sleep.hours: 24.0` (최대) | 정상 저장 | |
| 3 | `sleep.hours: -1.0` (음수) | 400 BAD_REQUEST | |
| 4 | `sleep.hours: 25.0` (24시간 초과) | 400 BAD_REQUEST | |
| 5 | `sleep.quality: 0` | 400 BAD_REQUEST (1~5 범위) | |
| 6 | `sleep.quality: 6` | 400 BAD_REQUEST | |

### TC-004-029: 경계값 테스트 - 식사
- **우선순위**: P1
- **유형**: Unit
- **사전 조건**: N/A
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `meals.count: 0` (최소) | 정상 저장 | |
| 2 | `meals.count: 5` (최대) | 정상 저장 | |
| 3 | `meals.count: -1` | 400 BAD_REQUEST | |
| 4 | `meals.count: 6` | 400 BAD_REQUEST | |
| 5 | `meals.quality: 0` | 400 BAD_REQUEST (1~5 범위) | |

### TC-004-030: 경계값 테스트 - 메모 길이
- **우선순위**: P2
- **유형**: Unit
- **사전 조건**: N/A
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `memo`: 500자 (최대) | 정상 저장 | |
| 2 | `memo`: 501자 (초과) | 400 BAD_REQUEST | |
| 3 | `mood.note`: 200자 (최대) | 정상 저장 | |
| 4 | `mood.note`: 201자 (초과) | 400 BAD_REQUEST | |

### TC-004-031: 신규 사용자 - 빈 추세 데이터
- **우선순위**: P1
- **유형**: Integration
- **사전 조건**: 건강 일지 0건인 신규 사용자
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `GET /api/health-journals` 호출 | 200 OK, 빈 배열 `[]` | |
| 2 | `GET /api/health-journals/trends?period=WEEK` | 400 BAD_REQUEST, 최소 데이터 부족 메시지 | |

### TC-004-032: 미래 날짜 건강 일지 작성 시도
- **우선순위**: P1
- **유형**: Unit
- **사전 조건**: N/A
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `POST /api/health-journals`, `date: "2026-03-20"` (미래 날짜) | 400 BAD_REQUEST, 미래 날짜 불가 | |

---

## 7. Kafka 이벤트 통합 테스트

### TC-004-033: 건강 일지 생성 -> Kafka 이벤트 -> Event Service 로깅
- **우선순위**: P0
- **유형**: Integration (E2E)
- **사전 조건**: Docker Compose 환경 기동
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `POST /api/health-journals` 호출 (건강 일지 생성) | health-journal-events 토픽에 이벤트 발행됨 | |
| 2 | Event Service가 이벤트 소비 | MongoDB에 이벤트 로그 기록됨 | |
| 3 | 이벤트 내용 확인 | `eventType: MOOD_RECORDED`, `userId`, `moodScore`, `healthScore`, `timestamp` 포함 | |

### TC-004-034: 체크인 + 감정 기록 -> checkin-events 토픽 하위 호환성
- **우선순위**: P0
- **유형**: Integration
- **사전 조건**: 기존 checkin-events 소비자 동작 중
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | mood 포함 체크인 수행 | checkin-events에 mood 데이터 포함된 이벤트 발행 | |
| 2 | 기존 소비자 확인 | mood 필드를 무시하고 정상 처리 (에러 미발생) | |
| 3 | mood 미포함 체크인 수행 | checkin-events에 기존 형식 이벤트 발행, 기존 소비자 정상 동작 | |

### TC-004-035: 건강 악화 감지 -> Kafka -> 관리자 알림 흐름
- **우선순위**: P0
- **유형**: Integration (E2E)
- **사전 조건**: 감정 3일 연속 2점 이하 데이터 존재
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | 배치 작업 실행 | health-alert-events 토픽에 이벤트 발행됨 | |
| 2 | Event Service가 이벤트 소비 | MongoDB health_alerts 컬렉션에 알림 저장됨 | |
| 3 | 관리자 대시보드에서 알림 조회 | `GET /api/admin/health-alerts`에서 조회 가능 | |

---

## 8. API 계약 테스트

### TC-004-036: 건강 일지 생성 - 요청 형식 검증
- **우선순위**: P0
- **유형**: Unit / Integration
- **사전 조건**: N/A
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | 유효한 JSON body로 POST 요청 | 201 CREATED 또는 200 OK | |
| 2 | 빈 body `{}` | 400 BAD_REQUEST (date 필수) | |
| 3 | 잘못된 날짜 형식 `"date": "14-03-2026"` | 400 BAD_REQUEST | |
| 4 | Content-Type 누락 | 415 Unsupported Media Type | |
| 5 | 잘못된 JSON 형식 | 400 BAD_REQUEST | |

### TC-004-037: 건강 일지 목록 조회 - 필수 파라미터 검증
- **우선순위**: P0
- **유형**: Integration
- **사전 조건**: N/A
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `startDate`, `endDate` 모두 제공 | 200 OK | |
| 2 | `startDate` 누락 | 400 BAD_REQUEST | |
| 3 | `endDate` 누락 | 400 BAD_REQUEST | |
| 4 | `startDate > endDate` | 400 BAD_REQUEST | |
| 5 | 날짜 형식 오류 | 400 BAD_REQUEST | |

### TC-004-038: 추세 조회 - period 파라미터 검증
- **우선순위**: P1
- **유형**: Integration
- **사전 조건**: N/A
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `period=WEEK` | 200 OK | |
| 2 | `period=TWO_WEEKS` | 200 OK | |
| 3 | `period=MONTH` | 200 OK | |
| 4 | `period=THREE_MONTHS` | 200 OK | |
| 5 | `period=YEAR` (미지원) | 400 BAD_REQUEST | |
| 6 | `period` 누락 | 400 BAD_REQUEST | |

### TC-004-039: 인증 필수 확인 - 모든 건강 일지 엔드포인트
- **우선순위**: P0
- **유형**: Integration
- **사전 조건**: N/A
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | JWT 없이 `POST /api/health-journals` | 401 Unauthorized | |
| 2 | JWT 없이 `GET /api/health-journals` | 401 Unauthorized | |
| 3 | JWT 없이 `GET /api/health-journals/{date}` | 401 Unauthorized | |
| 4 | JWT 없이 `GET /api/health-journals/trends` | 401 Unauthorized | |
| 5 | JWT 없이 `GET /api/health-journals/community-insights` | 401 Unauthorized | |
| 6 | JWT 없이 `DELETE /api/health-journals` | 401 Unauthorized | |
| 7 | 만료된 JWT로 호출 | 401 Unauthorized | |

### TC-004-040: 관리자 건강 알림 API - 권한 검증
- **우선순위**: P0
- **유형**: Integration
- **사전 조건**: N/A
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | Basic Auth 없이 `GET /api/admin/health-alerts` | 401 Unauthorized | |
| 2 | 일반 사용자 JWT로 호출 | 403 Forbidden | |
| 3 | 관리자 Basic Auth로 호출 | 200 OK | |
| 4 | `GET /api/admin/health-alerts?severity=HIGH` | 200 OK, HIGH만 반환 | |
| 5 | `GET /api/admin/health-alerts?page=0&size=10` | 200 OK, 페이징 적용 | |

### TC-004-041: 관리자 건강 알림 확인 처리
- **우선순위**: P1
- **유형**: Integration
- **사전 조건**: 미확인 건강 알림 존재
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `POST /api/admin/health-alerts/{alertId}/acknowledge`, body: `{"actionTaken": "전화 연락 완료"}` | 200 OK | |
| 2 | MongoDB 확인 | `acknowledged: true`, `acknowledgedBy`, `acknowledgedAt`, `actionTaken` 설정됨 | |
| 3 | 이미 확인된 알림 재확인 시도 | 400 BAD_REQUEST 또는 멱등성 처리 (200 OK) | |

---

## 9. 커뮤니티 인사이트

### TC-004-042: 커뮤니티 인사이트 조회
- **우선순위**: P1
- **유형**: Integration
- **사전 조건**: 동일 연령대 사용자 데이터 충분 (집계 완료)
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `GET /api/health-journals/community-insights` | 200 OK | |
| 2 | `ageGroup` 확인 | 사용자 연령대에 맞는 그룹 (예: "40대") | |
| 3 | `communityAverage` 확인 | `moodScore`, `healthScore`, `sleepHours`, `exerciseRate` 포함 | |
| 4 | `myAverage` 확인 | 내 평균 데이터 포함 | |
| 5 | `percentile` 확인 | 0~100 범위 백분위 | |
| 6 | 개인 식별 정보 미포함 확인 | 다른 사용자의 이름/ID 등 미노출 | |

### TC-004-043: 커뮤니티 인사이트 - 데이터 부족
- **우선순위**: P2
- **유형**: Integration
- **사전 조건**: 해당 연령대 사용자가 매우 적음 (5명 미만)
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | `GET /api/health-journals/community-insights` | 데이터 부족 안내 또는 통계가 부정확할 수 있다는 경고 | |

---

## 10. 다른 사용자 데이터 접근 불가 (보안)

### TC-004-044: 다른 사용자의 건강 일지 접근 차단
- **우선순위**: P0
- **유형**: Integration
- **사전 조건**: 사용자 A, 사용자 B 각각 건강 일지 존재
- **테스트 단계**:

| # | 입력/동작 | 기대 결과 | 통과 |
|---|----------|----------|------|
| 1 | 사용자 A의 JWT로 건강 일지 조회 | 사용자 A의 데이터만 반환 | |
| 2 | API가 user_id를 JWT에서 추출하는지 확인 | 요청 파라미터로 userId를 전달할 수 없음 (JWT 기반) | |

---

## 테스트 환경 요구사항

| 항목 | 요구사항 |
|------|---------|
| Docker Compose | PostgreSQL 16, MongoDB 7, Kafka (Confluent 7.5.0), Zookeeper |
| 테스트 데이터 | 최소 10명 사용자, 30일 건강 일지 이력, 다양한 감정/수면/식사 패턴 |
| 배치 테스트 | 건강 악화 감지 배치 트리거 가능한 환경 |
| 커뮤니티 통계 | 연령대별 최소 10명 이상 테스트 사용자 |

---

## 테스트 케이스 요약

| 구분 | 케이스 수 | P0 | P1 | P2 |
|------|----------|----|----|-----|
| 건강 일지 CRUD | TC-004-001 ~ TC-004-006 | 4 | 1 | 0 |
| 건강 점수 계산 | TC-004-007 ~ TC-004-009 | 2 | 1 | 0 |
| 감정 기록 (체크인 연동) | TC-004-010 ~ TC-004-012 | 3 | 0 | 0 |
| 추세 분석 | TC-004-013 ~ TC-004-017 | 2 | 2 | 1 |
| 건강 악화 감지 | TC-004-018 ~ TC-004-024 | 4 | 3 | 0 |
| 에지 케이스 / 경계값 | TC-004-025 ~ TC-004-032 | 1 | 5 | 1 |
| Kafka 통합 | TC-004-033 ~ TC-004-035 | 3 | 0 | 0 |
| API 계약 | TC-004-036 ~ TC-004-041 | 3 | 2 | 0 |
| 커뮤니티 인사이트 | TC-004-042 ~ TC-004-043 | 0 | 1 | 1 |
| 보안 | TC-004-044 | 1 | 0 | 0 |
| **합계** | **44건** | **23** | **15** | **3** |
