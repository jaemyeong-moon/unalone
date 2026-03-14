# 테스터팀 (QA Team)

## 역할
- 기능/통합/E2E 테스트 계획 수립 및 실행
- 버그 리포트 작성 및 재현 시나리오 정리
- 테스트 커버리지 관리
- API 테스트 및 성능 검증

## 테스트 범위

### Backend 테스트
| 서비스 | 테스트 대상 | 도구 |
|--------|-----------|------|
| api-service | Auth, CheckIn, Community, Guardian, Profile API | JUnit 5, MockMvc, Mockito |
| admin-service | Dashboard, User, Alert, Product, Order API | JUnit 5, MockMvc, Mockito |
| event-service | Kafka Consumer, Alert 생성 로직 | JUnit 5, EmbeddedKafka |
| common | DTO 직렬화, Event 클래스 | JUnit 5 |

### Frontend 테스트
| 대상 | 도구 |
|------|------|
| 컴포넌트 단위 | Jest, React Testing Library |
| 페이지 렌더링 | Jest + Next.js test utils |
| E2E | Playwright 또는 Cypress (도입 시) |

### API 통합 테스트
| 항목 | 검증 내용 |
|------|----------|
| 인증 플로우 | 회원가입 → 로그인 → JWT 발급 → 인증 API 호출 |
| 체크인 플로우 | 체크인 → Kafka 이벤트 → Event Service 처리 → Alert 생성 |
| 관리자 플로우 | 대시보드 조회 → 사용자 관리 → 알림 해결 |
| 상품/주문 | 상품 등록 → 주문 → 재고 차감 → 동시성 검증 |

## 테스트 실행 방법
```bash
# Backend 전체 테스트
cd backend && ./gradlew test

# 특정 서비스 테스트
cd backend && ./gradlew :api-service:test
cd backend && ./gradlew :admin-service:test
cd backend && ./gradlew :event-service:test

# Frontend 테스트
cd frontend && npm test
cd frontend && npm run test:coverage

# Docker 환경에서 통합 테스트
docker compose up -d
# API 테스트 스크립트 실행
```

## 버그 리포트 형식
```markdown
## [BUG-{번호}] {버그 제목}

- **심각도**: Critical / Major / Minor / Trivial
- **재현 환경**: OS, 브라우저, 서비스 버전
- **재현 단계**:
  1. Step 1
  2. Step 2
  3. Step 3
- **기대 결과**: 정상 동작 설명
- **실제 결과**: 발생한 문제 설명
- **스크린샷/로그**: (해당 시)
- **관련 서비스**: api-service / admin-service / event-service / frontend
```

## 테스트 케이스 형식
```markdown
## [TC-{번호}] {테스트 케이스명}

- **기능**: 관련 기능 명세 (FEAT-xxx)
- **유형**: Unit / Integration / E2E
- **사전 조건**: 테스트 전 필요한 상태
- **테스트 단계**:
  | # | 입력/동작 | 기대 결과 | 통과 |
  |---|----------|----------|------|
  | 1 | 동작 설명 | 기대 결과 | ✅/❌ |
- **테스트 데이터**: 필요한 테스트 데이터 명시
```

## 작업 규칙
- 버그 리포트: `docs/qa/bugs/` 디렉토리에 저장 (`BUG-{번호}-{제목}.md`)
- 테스트 케이스: `docs/qa/testcases/` 디렉토리에 저장 (`TC-{번호}-{제목}.md`)
- 테스트 결과 리포트: `docs/qa/reports/` 디렉토리에 저장
- 백엔드 테스트 코드: 각 서비스의 `src/test/` 디렉토리
- 프론트엔드 테스트 코드: `src/__tests__/` 또는 컴포넌트 옆 `.test.tsx`
- 새 기능 구현 시 반드시 테스트 케이스 작성 후 개발팀에 전달
- Critical 버그 발견 시 즉시 리포트 + 관련 팀에 알림

## 품질 기준
- 백엔드 테스트 커버리지 목표: 80% 이상
- 모든 API 엔드포인트에 대한 Happy Path + Error Path 테스트 필수
- Kafka 이벤트 흐름 통합 테스트 필수
- 동시성 관련 기능(Product 재고)은 부하 테스트 포함
