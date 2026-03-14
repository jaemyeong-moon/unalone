-- =====================================================================
-- V2: 스마트 체크인 / 건강 저널 / 돌봄 매칭 테이블 추가
-- FEAT-002: 스마트 체크인 스케줄 및 에스컬레이션
-- FEAT-003: 건강 저널
-- FEAT-004: 돌봄 매칭 (봉사자, 매칭, 방문)
-- =====================================================================

-- -----------------------------------------------
-- 1. checkins 테이블에 mood_score 컬럼 추가 (FEAT-003 하위 호환)
-- 기분 점수: 1~10 범위, 체크인 시 선택적으로 기록
-- -----------------------------------------------
ALTER TABLE checkins
    ADD COLUMN IF NOT EXISTS mood_score INTEGER;

COMMENT ON COLUMN checkins.mood_score IS '기분 점수 (1~10), 체크인 시 선택 입력';

-- -----------------------------------------------
-- 2. 체크인 스케줄 테이블 (FEAT-002)
-- 사용자별 체크인 주기/시간대/활성 요일을 관리
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS checkin_schedules (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL UNIQUE,
    interval_hours  INTEGER         NOT NULL DEFAULT 24,
    preferred_time  TIME            NOT NULL DEFAULT '09:00:00',
    active_days     VARCHAR(100)    NOT NULL DEFAULT 'MON,TUE,WED,THU,FRI,SAT,SUN',
    pause_until     DATE,
    next_check_in_due TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_checkin_schedule_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

COMMENT ON TABLE  checkin_schedules IS '체크인 스케줄 - 사용자별 체크인 주기 설정';
COMMENT ON COLUMN checkin_schedules.interval_hours IS '체크인 간격 (시간 단위)';
COMMENT ON COLUMN checkin_schedules.preferred_time IS '선호 체크인 시각';
COMMENT ON COLUMN checkin_schedules.active_days IS '활성 요일 (쉼표 구분: MON,TUE,...)';
COMMENT ON COLUMN checkin_schedules.pause_until IS '일시정지 종료일 (NULL이면 활성)';
COMMENT ON COLUMN checkin_schedules.next_check_in_due IS '다음 체크인 예정 시각';

-- 다음 체크인 예정 시각 기준 미응답 조회용 인덱스
CREATE INDEX IF NOT EXISTS idx_checkin_schedules_next_due
    ON checkin_schedules(next_check_in_due);

-- -----------------------------------------------
-- 3. 에스컬레이션 테이블 (FEAT-002)
-- 미응답 시 단계별 알림 이력 관리
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS escalations (
    id                   BIGSERIAL       PRIMARY KEY,
    user_id              BIGINT          NOT NULL,
    checkin_schedule_id  BIGINT,
    stage                VARCHAR(20)     NOT NULL DEFAULT 'REMINDER',
    triggered_at         TIMESTAMP       NOT NULL DEFAULT NOW(),
    resolved_at          TIMESTAMP,
    resolved             BOOLEAN         NOT NULL DEFAULT FALSE,
    notified_contacts    VARCHAR(500),
    created_at           TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_escalation_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

COMMENT ON TABLE  escalations IS '에스컬레이션 - 미응답 시 단계별 알림 이력';
COMMENT ON COLUMN escalations.stage IS '에스컬레이션 단계: REMINDER, WARNING, DANGER, CRITICAL';
COMMENT ON COLUMN escalations.triggered_at IS '에스컬레이션 발생 시각';
COMMENT ON COLUMN escalations.resolved IS '해결 여부';
COMMENT ON COLUMN escalations.notified_contacts IS '알림 발송된 연락처 목록';

-- 사용자별 미해결 에스컬레이션 조회용
CREATE INDEX IF NOT EXISTS idx_escalations_user_resolved
    ON escalations(user_id, resolved);

-- 단계별 조회
CREATE INDEX IF NOT EXISTS idx_escalations_stage
    ON escalations(stage);

-- 발생 시각 기준 조회
CREATE INDEX IF NOT EXISTS idx_escalations_triggered_at
    ON escalations(triggered_at);

-- -----------------------------------------------
-- 4. 건강 저널 테이블 (FEAT-003)
-- 일별 건강 기록 (수면, 식사, 운동, 증상 등)
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS health_journals (
    id               BIGSERIAL       PRIMARY KEY,
    user_id          BIGINT          NOT NULL,
    journal_date     DATE            NOT NULL,
    mood_score       INTEGER,
    sleep_hours      NUMERIC(4,2),
    sleep_quality    INTEGER,
    meal_count       INTEGER,
    meal_quality     INTEGER,
    exercise_minutes INTEGER,
    exercise_type    VARCHAR(50),
    symptoms         VARCHAR(500),
    medications      VARCHAR(500),
    pain_level       INTEGER,
    notes            VARCHAR(500),
    health_score     INTEGER,
    created_at       TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_health_journal_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    -- 사용자별 날짜 중복 방지
    CONSTRAINT uq_health_journal_user_date
        UNIQUE (user_id, journal_date)
);

COMMENT ON TABLE  health_journals IS '건강 저널 - 일별 건강 기록';
COMMENT ON COLUMN health_journals.mood_score IS '기분 점수 (1~10)';
COMMENT ON COLUMN health_journals.sleep_hours IS '수면 시간';
COMMENT ON COLUMN health_journals.sleep_quality IS '수면 질 (1~5)';
COMMENT ON COLUMN health_journals.meal_count IS '식사 횟수';
COMMENT ON COLUMN health_journals.meal_quality IS '식사 질 (1~5)';
COMMENT ON COLUMN health_journals.exercise_minutes IS '운동 시간 (분)';
COMMENT ON COLUMN health_journals.exercise_type IS '운동 유형';
COMMENT ON COLUMN health_journals.symptoms IS '증상 기록';
COMMENT ON COLUMN health_journals.medications IS '복용 약물';
COMMENT ON COLUMN health_journals.pain_level IS '통증 수준 (1~10)';
COMMENT ON COLUMN health_journals.health_score IS '종합 건강 점수 (자동 산출)';

-- 날짜 범위 조회용 (트렌드 분석)
CREATE INDEX IF NOT EXISTS idx_health_journals_user_date
    ON health_journals(user_id, journal_date);

-- -----------------------------------------------
-- 5. 봉사자 테이블 (FEAT-004)
-- 돌봄 봉사자 프로필 및 활동 가능 정보
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS volunteers (
    id                   BIGSERIAL       PRIMARY KEY,
    user_id              BIGINT          NOT NULL UNIQUE,
    available_days       VARCHAR(100),
    available_time_start TIME            NOT NULL,
    available_time_end   TIME            NOT NULL,
    radius               DOUBLE PRECISION NOT NULL DEFAULT 2.0,
    latitude             DOUBLE PRECISION NOT NULL,
    longitude            DOUBLE PRECISION NOT NULL,
    introduction         VARCHAR(500),
    status               VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    trust_score          INTEGER         NOT NULL DEFAULT 50,
    total_visits         INTEGER         NOT NULL DEFAULT 0,
    approved_at          TIMESTAMP,
    created_at           TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP       NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  volunteers IS '봉사자 - 돌봄 봉사자 프로필';
COMMENT ON COLUMN volunteers.available_days IS '활동 가능 요일 (쉼표 구분)';
COMMENT ON COLUMN volunteers.radius IS '활동 반경 (km)';
COMMENT ON COLUMN volunteers.latitude IS '봉사자 위치 위도';
COMMENT ON COLUMN volunteers.longitude IS '봉사자 위치 경도';
COMMENT ON COLUMN volunteers.status IS '상태: PENDING, APPROVED, SUSPENDED, WITHDRAWN';
COMMENT ON COLUMN volunteers.trust_score IS '신뢰 점수 (0~100)';
COMMENT ON COLUMN volunteers.total_visits IS '총 방문 횟수';

-- 상태별 조회
CREATE INDEX IF NOT EXISTS idx_volunteers_status
    ON volunteers(status);

-- 위치 기반 근처 봉사자 검색용 (위도/경도)
CREATE INDEX IF NOT EXISTS idx_volunteers_location
    ON volunteers(latitude, longitude);

-- 신뢰 점수 기준 정렬 조회
CREATE INDEX IF NOT EXISTS idx_volunteers_trust_score
    ON volunteers(trust_score);

-- -----------------------------------------------
-- 6. 돌봄 매칭 테이블 (FEAT-004)
-- 봉사자-돌봄대상자 매칭 관리
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS care_matches (
    id              BIGSERIAL       PRIMARY KEY,
    volunteer_id    BIGINT          NOT NULL,
    receiver_id     BIGINT          NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    matched_at      TIMESTAMP,
    completed_at    TIMESTAMP,
    distance        DOUBLE PRECISION,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  care_matches IS '돌봄 매칭 - 봉사자와 돌봄대상자 매칭';
COMMENT ON COLUMN care_matches.status IS '매칭 상태: PENDING, ACTIVE, COMPLETED, CANCELLED';
COMMENT ON COLUMN care_matches.distance IS '봉사자-대상자 간 거리 (km)';

-- 봉사자별 매칭 조회
CREATE INDEX IF NOT EXISTS idx_care_matches_volunteer
    ON care_matches(volunteer_id, status);

-- 돌봄대상자별 매칭 조회
CREATE INDEX IF NOT EXISTS idx_care_matches_receiver
    ON care_matches(receiver_id, status);

-- -----------------------------------------------
-- 7. 돌봄 방문 테이블 (FEAT-004)
-- 봉사자의 방문 일정 및 보고서 관리
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS care_visits (
    id                 BIGSERIAL       PRIMARY KEY,
    care_match_id      BIGINT          NOT NULL,
    volunteer_id       BIGINT          NOT NULL,
    receiver_id        BIGINT          NOT NULL,
    scheduled_date     DATE            NOT NULL,
    scheduled_time     TIME            NOT NULL,
    status             VARCHAR(20)     NOT NULL DEFAULT 'SCHEDULED',
    report_content     VARCHAR(2000),
    receiver_condition VARCHAR(20),
    special_notes      VARCHAR(1000),
    visited_at         TIMESTAMP,
    created_at         TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP       NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  care_visits IS '돌봄 방문 - 방문 일정 및 보고서';
COMMENT ON COLUMN care_visits.status IS '방문 상태: SCHEDULED, COMPLETED, CANCELLED, NO_SHOW';
COMMENT ON COLUMN care_visits.receiver_condition IS '돌봄대상자 상태: GOOD, FAIR, POOR, CRITICAL';
COMMENT ON COLUMN care_visits.report_content IS '방문 보고서 내용';
COMMENT ON COLUMN care_visits.special_notes IS '특이사항';

-- 봉사자별 방문 일정 조회
CREATE INDEX IF NOT EXISTS idx_care_visits_volunteer_date
    ON care_visits(volunteer_id, scheduled_date);

-- 돌봄대상자별 방문 일정 조회
CREATE INDEX IF NOT EXISTS idx_care_visits_receiver_date
    ON care_visits(receiver_id, scheduled_date);

-- 매칭별 방문 조회
CREATE INDEX IF NOT EXISTS idx_care_visits_match
    ON care_visits(care_match_id);

-- 상태별 날짜 범위 조회 (대시보드용)
CREATE INDEX IF NOT EXISTS idx_care_visits_status_date
    ON care_visits(status, scheduled_date);
