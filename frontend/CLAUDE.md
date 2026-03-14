# Frontend - Next.js 애플리케이션

## 기술 스택
- Next.js 14.2 (App Router)
- React 18
- TypeScript
- Tailwind CSS

## 디렉토리 구조
```
src/
  app/                  # App Router 페이지
    admin/              # 관리자 페이지 (대시보드, 사용자, 알림)
    community/          # 커뮤니티 (목록, 상세)
    guardians/          # 보호자 관리
    login/              # 로그인
    profile/            # 프로필
    signup/             # 회원가입
    layout.tsx          # 루트 레이아웃
    page.tsx            # 홈 (랜딩/대시보드)
  components/common/    # 공통 컴포넌트 (Header 등)
  lib/                  # API 클라이언트, 인증 유틸리티
  types/                # TypeScript 타입 정의
```

## 코딩 컨벤션
- 컴포넌트: 함수형 컴포넌트 + React Hooks
- 서버/클라이언트 구분: `'use client'` 디렉티브 명시
- 스타일링: Tailwind CSS 유틸리티 클래스 사용, 인라인 스타일 지양
- 타입: `src/types/`에 공통 타입 정의, 컴포넌트 props는 인터페이스로 정의
- API 호출: `src/lib/` 내 API 클라이언트를 통해 호출
- 인증: JWT 토큰 기반, `src/lib/` 내 인증 유틸리티 사용

## API 프록시
- `next.config.mjs`에서 백엔드 서비스로 리라이트 설정
  - `/api/*` → API Service (:8080)
  - `/admin/*` → Admin Service (:8081)
  - `/events/*` → Event Service (:8082)

## 페이지별 인증 요구사항
- 공개: `/login`, `/signup`, `/community` (목록)
- JWT 필요: `/`, `/profile`, `/guardians`, `/community` (작성/삭제)
- Basic Auth: `/admin/*` (관리자 전용)

## 빌드 & 실행
```bash
npm install
npm run dev          # 개발 서버 (:3000)
npm run build        # 프로덕션 빌드
npm run start        # 프로덕션 서버
npm run lint         # ESLint
```

## 주의사항
- 백엔드 API 스키마 변경 시 `src/types/`와 API 클라이언트 동시 수정
- SSR 페이지에서 인증 토큰 처리 시 쿠키/헤더 전달 주의
- Tailwind 커스텀 설정: `tailwind.config.ts` 참조
