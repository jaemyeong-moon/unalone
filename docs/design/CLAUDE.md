# 디자인팀 (Design Team)

## 역할
- UI/UX 설계 및 화면 구성 정의
- 컴포넌트 설계 및 디자인 시스템 관리
- 사용자 플로우 및 인터랙션 설계
- 반응형 디자인 및 접근성(A11y) 가이드

## 현재 디자인 스택
- **스타일링**: Tailwind CSS (유틸리티 클래스 기반)
- **컴포넌트**: React 함수형 컴포넌트
- **레이아웃**: Next.js App Router 기반 중첩 레이아웃
- **아이콘/에셋**: `public/` 디렉토리

## 페이지 구성 현황
| 페이지 | 경로 | 컴포넌트 위치 |
|--------|------|--------------|
| 홈 (랜딩/대시보드) | `/` | `src/app/page.tsx` |
| 로그인 | `/login` | `src/app/login/page.tsx` |
| 회원가입 | `/signup` | `src/app/signup/page.tsx` |
| 커뮤니티 목록 | `/community` | `src/app/community/page.tsx` |
| 게시글 상세 | `/community/[id]` | `src/app/community/[id]/page.tsx` |
| 프로필 | `/profile` | `src/app/profile/page.tsx` |
| 보호자 관리 | `/guardians` | `src/app/guardians/page.tsx` |
| 관리자 대시보드 | `/admin` | `src/app/admin/page.tsx` |
| 사용자 관리 | `/admin/users` | `src/app/admin/users/page.tsx` |
| 알림 관리 | `/admin/alerts` | `src/app/admin/alerts/page.tsx` |

## 디자인 원칙
1. **심플하고 직관적**: 고령 사용자도 쉽게 사용할 수 있는 UI
2. **명확한 시각적 피드백**: 체크인 상태, 알림 레벨을 색상/아이콘으로 구분
3. **일관된 컴포넌트**: 공통 Header, Button, Card, Form 패턴 재사용
4. **반응형**: 모바일 우선 (1인 가구 대상 → 모바일 사용률 높음)
5. **접근성**: 충분한 색상 대비, 키보드 네비게이션, aria 속성

## 색상 가이드
| 용도 | 색상 | 적용 |
|------|------|------|
| Primary | Blue 계열 | 주요 버튼, 링크, 강조 |
| Success/Safe | Green | 체크인 완료, 정상 상태 |
| Warning | Yellow/Amber | WARNING 알림 레벨 |
| Danger | Orange | DANGER 알림 레벨 |
| Critical | Red | CRITICAL 알림 레벨, 에러 |
| Neutral | Gray | 배경, 보조 텍스트, 비활성 |

## 산출물 형식

### 화면 설계서
```markdown
## [페이지명] 화면 설계

### 레이아웃
- 구조 설명 (Header, Main, Sidebar 등)
- 반응형 브레이크포인트별 변화

### 컴포넌트 목록
| 컴포넌트 | 설명 | Props |
|----------|------|-------|
| ComponentName | 설명 | prop1, prop2 |

### 인터랙션
- 사용자 액션 → 결과 플로우
- 로딩/에러/빈 상태 처리

### Tailwind 클래스 가이드
- 사용할 주요 클래스 명시
```

## 작업 규칙
- 화면 설계서는 `docs/design/screens/` 디렉토리에 저장
- 파일명: `SCREEN-{페이지명}.md` (예: `SCREEN-checkin-dashboard.md`)
- 새 컴포넌트는 `src/components/` 하위에 배치
- Tailwind 커스텀 값 추가 시 `tailwind.config.ts` 수정 명시
- 기획팀 명세의 수용 기준이 UI에 반영되었는지 확인
