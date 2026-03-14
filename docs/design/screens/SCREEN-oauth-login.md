# OAuth 소셜 로그인 화면 설계

## 개요
기존 이메일/패스워드 인증 방식에 카카오, 구글 OAuth 소셜 로그인을 추가한다.
고령 사용자 및 1인 가구 대상 서비스 특성상 가입 장벽을 낮추고, 친숙한 소셜 계정으로 빠르게 접근할 수 있도록 한다.

## 영향받는 페이지
| 페이지 | 경로 | 변경 유형 |
|--------|------|-----------|
| 로그인 | `/login` | 기존 페이지 수정 |
| 회원가입 | `/signup` | 기존 페이지 수정 |
| OAuth 콜백 | `/auth/callback` | 신규 페이지 |

---

## 1. 로그인 페이지 (`/login`) 변경

### 레이아웃
기존 구조를 유지하되, 이메일/패스워드 폼 **아래**에 구분선과 소셜 로그인 버튼 영역을 추가한다.

```
[Unalone 로고 + 타이틀]
[에러 배너 (조건부)]
[이메일 입력]
[비밀번호 입력]
[로그인 버튼]
─── 또는 소셜 계정으로 로그인 ───
[카카오 로그인 버튼]
[구글 로그인 버튼]
[회원가입 링크]
```

### 추가되는 마크업 구조
`</form>` 태그와 회원가입 링크 `<p>` 사이에 아래 영역을 삽입한다.

```tsx
{/* 구분선 */}
<div className="my-6 flex items-center gap-3">
  <div className="flex-1 h-px bg-gray-200" />
  <span className="text-sm text-gray-400 whitespace-nowrap">또는 소셜 계정으로 로그인</span>
  <div className="flex-1 h-px bg-gray-200" />
</div>

{/* 소셜 로그인 버튼 */}
<div className="space-y-3">
  <SocialLoginButton provider="kakao" />
  <SocialLoginButton provider="google" />
</div>
```

### 반응형 대응
- 카드 컨테이너 `max-w-md w-full` 유지 (모바일에서 좌우 패딩으로 자연 축소)
- 소셜 버튼은 풀 너비(`w-full`)로 터치 영역 확보

---

## 2. 회원가입 페이지 (`/signup`) 변경

### 레이아웃
소셜 로그인 버튼을 **상단**에 배치하여, 복잡한 폼 입력 전에 간편 가입 옵션을 먼저 제시한다.

```
[Unalone 로고 + 타이틀]
[에러 배너 (조건부)]
[카카오로 시작하기 버튼]
[구글로 시작하기 버튼]
─── 또는 이메일로 가입 ───
[이름 입력]
[이메일 입력]
[비밀번호 입력]
[전화번호 입력 (선택)]
[회원가입 버튼]
[로그인 링크]
```

### 추가되는 마크업 구조
에러 배너와 `<form>` 사이에 아래 영역을 삽입한다.

```tsx
{/* 소셜 가입 버튼 */}
<div className="space-y-3">
  <SocialLoginButton provider="kakao" label="카카오로 시작하기" />
  <SocialLoginButton provider="google" label="구글로 시작하기" />
</div>

{/* 구분선 */}
<div className="my-6 flex items-center gap-3">
  <div className="flex-1 h-px bg-gray-200" />
  <span className="text-sm text-gray-400 whitespace-nowrap">또는 이메일로 가입</span>
  <div className="flex-1 h-px bg-gray-200" />
</div>
```

### 반응형 대응
- 로그인 페이지와 동일한 카드 레이아웃 유지
- 소셜 버튼이 상단에 위치하므로 모바일에서 스크롤 없이 즉시 노출

---

## 3. OAuth 콜백 페이지 (`/auth/callback`) - 신규

### 경로
`frontend/src/app/auth/callback/page.tsx`

### 레이아웃
전체 화면 중앙 정렬, 로딩 상태만 표시하는 최소 UI.

```
[전체 화면 중앙]
  [LoadingSpinner]
  [로그인 중... 텍스트]
  [잠시만 기다려 주세요 보조 텍스트]
```

### 마크업 구조

```tsx
'use client';

import { useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Suspense } from 'react';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import { saveAuth } from '@/lib/auth';
import apiClient from '@/lib/api';

function CallbackContent() {
  const router = useRouter();
  const searchParams = useSearchParams();

  useEffect(() => {
    const code = searchParams.get('code');
    const provider = searchParams.get('provider');
    const error = searchParams.get('error');

    if (error) {
      router.replace(`/login?error=${encodeURIComponent('소셜 로그인에 실패했습니다')}`);
      return;
    }

    if (code && provider) {
      apiClient
        .post(`/api/auth/oauth/${provider}`, { code })
        .then((res) => {
          saveAuth(res.data.data);
          router.replace('/');
        })
        .catch(() => {
          router.replace(`/login?error=${encodeURIComponent('소셜 로그인 처리 중 오류가 발생했습니다')}`);
        });
    }
  }, [searchParams, router]);

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
      <LoadingSpinner className="mb-4" />
      <p className="text-lg font-medium text-gray-900">로그인 중...</p>
      <p className="text-sm text-gray-500 mt-1">잠시만 기다려 주세요</p>
    </div>
  );
}

export default function OAuthCallbackPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
        <LoadingSpinner className="mb-4" />
        <p className="text-lg font-medium text-gray-900">로그인 중...</p>
        <p className="text-sm text-gray-500 mt-1">잠시만 기다려 주세요</p>
      </div>
    }>
      <CallbackContent />
    </Suspense>
  );
}
```

### 상태 처리
| 상태 | 동작 |
|------|------|
| 정상 | 로딩 스피너 표시 -> API 호출 -> `saveAuth` -> `/` 리다이렉트 |
| 에러 (OAuth provider 에러) | `/login?error=소셜 로그인에 실패했습니다` 로 리다이렉트 |
| 에러 (서버 처리 실패) | `/login?error=소셜 로그인 처리 중 오류가 발생했습니다` 로 리다이렉트 |

---

## 4. 컴포넌트 목록

### 4-1. SocialLoginButton (신규)

**파일 위치**: `frontend/src/components/common/SocialLoginButton.tsx`

#### Props 인터페이스

```typescript
interface SocialLoginButtonProps {
  provider: 'kakao' | 'google';
  label?: string;       // 커스텀 라벨 (기본값: provider별 자동 생성)
  disabled?: boolean;
  className?: string;
}
```

#### 기본 라벨
| provider | 기본 label |
|----------|-----------|
| `kakao` | `카카오 로그인` |
| `google` | `구글 로그인` |

#### 디자인 스펙

**카카오 버튼**
| 속성 | 값 |
|------|-----|
| 배경색 | `#FEE500` (카카오 공식 색상) |
| 텍스트 색상 | `#000000` (rgba 0.85 불투명도) |
| 아이콘 | 카카오 말풍선 SVG (18x18) |
| hover | 밝기 95% (`brightness-95`) |

```
Tailwind 클래스:
w-full h-12 flex items-center justify-center gap-2
rounded-lg font-medium text-sm
transition-all duration-200
hover:brightness-95
disabled:opacity-50 disabled:cursor-not-allowed

인라인 스타일:
backgroundColor: '#FEE500'
color: 'rgba(0, 0, 0, 0.85)'
```

**구글 버튼**
| 속성 | 값 |
|------|-----|
| 배경색 | `#FFFFFF` |
| 텍스트 색상 | `text-gray-700` |
| 테두리 | `border border-gray-300` |
| 아이콘 | 구글 G 로고 SVG (18x18) |
| hover | `hover:bg-gray-50` |

```
Tailwind 클래스:
w-full h-12 flex items-center justify-center gap-2
bg-white border border-gray-300 rounded-lg
font-medium text-sm text-gray-700
transition-all duration-200
hover:bg-gray-50
disabled:opacity-50 disabled:cursor-not-allowed
```

#### 클릭 동작
버튼 클릭 시 OAuth 인증 URL로 리다이렉트한다.

```typescript
const OAUTH_URLS: Record<string, string> = {
  kakao: `/api/auth/oauth/kakao`,
  google: `/api/auth/oauth/google`,
};

const handleClick = () => {
  window.location.href = OAUTH_URLS[provider];
};
```

#### 전체 컴포넌트 코드

```tsx
'use client';

interface SocialLoginButtonProps {
  provider: 'kakao' | 'google';
  label?: string;
  disabled?: boolean;
  className?: string;
}

const DEFAULT_LABELS: Record<string, string> = {
  kakao: '카카오 로그인',
  google: '구글 로그인',
};

const OAUTH_URLS: Record<string, string> = {
  kakao: '/api/auth/oauth/kakao',
  google: '/api/auth/oauth/google',
};

function KakaoIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path
        fillRule="evenodd"
        clipRule="evenodd"
        d="M9 0.6C4.029 0.6 0 3.726 0 7.554C0 9.918 1.557 12.006 3.933 13.212L2.934 16.77C2.862 17.022 3.15 17.226 3.372 17.082L7.596 14.37C8.058 14.424 8.526 14.454 9 14.454C13.971 14.454 18 11.328 18 7.554C18 3.726 13.971 0.6 9 0.6Z"
        fill="black"
      />
    </svg>
  );
}

function GoogleIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 18 18" xmlns="http://www.w3.org/2000/svg">
      <path d="M17.64 9.205c0-.639-.057-1.252-.164-1.841H9v3.481h4.844a4.14 4.14 0 01-1.796 2.716v2.259h2.908c1.702-1.567 2.684-3.875 2.684-6.615z" fill="#4285F4"/>
      <path d="M9 18c2.43 0 4.467-.806 5.956-2.18l-2.908-2.259c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332A8.997 8.997 0 009 18z" fill="#34A853"/>
      <path d="M3.964 10.71A5.41 5.41 0 013.682 9c0-.593.102-1.17.282-1.71V4.958H.957A8.996 8.996 0 000 9c0 1.452.348 2.827.957 4.042l3.007-2.332z" fill="#FBBC05"/>
      <path d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0A8.997 8.997 0 00.957 4.958L3.964 6.29C4.672 4.163 6.656 3.58 9 3.58z" fill="#EA4335"/>
    </svg>
  );
}

export default function SocialLoginButton({
  provider,
  label,
  disabled = false,
  className = '',
}: SocialLoginButtonProps) {
  const displayLabel = label || DEFAULT_LABELS[provider];

  const handleClick = () => {
    if (!disabled) {
      window.location.href = OAUTH_URLS[provider];
    }
  };

  if (provider === 'kakao') {
    return (
      <button
        type="button"
        onClick={handleClick}
        disabled={disabled}
        className={`w-full h-12 flex items-center justify-center gap-2 rounded-lg font-medium text-sm transition-all duration-200 hover:brightness-95 disabled:opacity-50 disabled:cursor-not-allowed ${className}`}
        style={{ backgroundColor: '#FEE500', color: 'rgba(0, 0, 0, 0.85)' }}
        aria-label={displayLabel}
      >
        <KakaoIcon />
        {displayLabel}
      </button>
    );
  }

  return (
    <button
      type="button"
      onClick={handleClick}
      disabled={disabled}
      className={`w-full h-12 flex items-center justify-center gap-2 bg-white border border-gray-300 rounded-lg font-medium text-sm text-gray-700 transition-all duration-200 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed ${className}`}
      aria-label={displayLabel}
    >
      <GoogleIcon />
      {displayLabel}
    </button>
  );
}
```

### 4-2. OAuthDivider (신규)

**파일 위치**: `SocialLoginButton`과 함께 사용하거나, 인라인으로 직접 마크업 가능 (별도 컴포넌트 분리는 선택사항)

구분선은 반복 사용되므로 인라인 마크업으로 충분하나, 필요 시 아래와 같이 분리할 수 있다.

```typescript
interface OAuthDividerProps {
  text: string;  // 예: "또는 소셜 계정으로 로그인"
}
```

```tsx
export default function OAuthDivider({ text }: OAuthDividerProps) {
  return (
    <div className="my-6 flex items-center gap-3">
      <div className="flex-1 h-px bg-gray-200" />
      <span className="text-sm text-gray-400 whitespace-nowrap">{text}</span>
      <div className="flex-1 h-px bg-gray-200" />
    </div>
  );
}
```

---

## 5. 인터랙션 플로우

### 5-1. 소셜 로그인 성공 플로우
```
사용자: 소셜 로그인 버튼 클릭
  -> 브라우저: /api/auth/oauth/{provider} 로 이동
  -> 백엔드: 해당 OAuth provider의 인증 URL로 302 리다이렉트
  -> 외부: 카카오/구글 인증 화면에서 사용자 동의
  -> 브라우저: /auth/callback?code=xxx&provider=kakao 로 콜백
  -> 콜백 페이지: 로딩 스피너 표시, POST /api/auth/oauth/{provider} { code }
  -> 백엔드: 토큰 발급 응답
  -> 콜백 페이지: saveAuth() 후 / 대시보드로 router.replace
```

### 5-2. 소셜 로그인 실패 플로우
```
사용자: 소셜 로그인 버튼 클릭
  -> (인증 과정 중 에러 발생)
  -> 브라우저: /auth/callback?error=access_denied 로 콜백
  -> 콜백 페이지: /login?error=소셜 로그인에 실패했습니다 로 리다이렉트
  -> 로그인 페이지: AlertBanner에 에러 메시지 표시
```

### 5-3. 로그인 페이지 에러 파라미터 처리
기존 로그인 페이지에서 URL 쿼리 파라미터 `error`를 읽어 `AlertBanner`에 표시하는 로직 추가 필요.

```tsx
// login/page.tsx 에 추가
const searchParams = useSearchParams();

useEffect(() => {
  const errorParam = searchParams.get('error');
  if (errorParam) {
    setError(decodeURIComponent(errorParam));
  }
}, [searchParams]);
```

---

## 6. 접근성 (A11y)

| 항목 | 적용 내용 |
|------|-----------|
| `aria-label` | 소셜 버튼에 `aria-label` 속성으로 목적 명시 |
| 키보드 | `<button>` 요소 사용으로 Enter/Space 키 네이티브 지원 |
| 색상 대비 | 카카오: 검정 텍스트 on 노란 배경 (WCAG AA 충족), 구글: 회색 텍스트 on 흰 배경 + 테두리 |
| 포커스 링 | 브라우저 기본 포커스 링 유지 (Tailwind reset 확인 필요, 필요 시 `focus:ring-2 focus:ring-offset-2` 추가) |
| 로딩 상태 | 콜백 페이지에서 시각적 로딩 표시 (스피너 + 텍스트) |

---

## 7. 반응형 브레이크포인트별 대응

| 브레이크포인트 | 변화 |
|---------------|------|
| 모바일 (< 640px) | 카드 좌우 패딩 `p-6`으로 축소, 버튼 높이 `h-12` 유지 (터치 영역 48px) |
| 태블릿 이상 (>= 640px) | 카드 `max-w-md` 중앙 정렬, 패딩 `p-8` |

버튼 높이 `h-12` (48px)는 모든 브레이크포인트에서 동일하게 유지하여 모바일 터치 가이드라인(최소 44px)을 충족한다.
기존 이메일 로그인 버튼(`py-2.5`)보다 소셜 버튼이 약간 크지만, 소셜 버튼은 아이콘 + 텍스트를 포함하므로 시각적 균형이 맞다.

---

## 8. Tailwind 클래스 가이드

### 구분선
```
my-6 flex items-center gap-3       // 컨테이너
flex-1 h-px bg-gray-200            // 선
text-sm text-gray-400 whitespace-nowrap  // 텍스트
```

### 카카오 버튼
```
w-full h-12 flex items-center justify-center gap-2
rounded-lg font-medium text-sm
transition-all duration-200
hover:brightness-95
disabled:opacity-50 disabled:cursor-not-allowed
+ style={{ backgroundColor: '#FEE500', color: 'rgba(0, 0, 0, 0.85)' }}
```

### 구글 버튼
```
w-full h-12 flex items-center justify-center gap-2
bg-white border border-gray-300 rounded-lg
font-medium text-sm text-gray-700
transition-all duration-200
hover:bg-gray-50
disabled:opacity-50 disabled:cursor-not-allowed
```

### 콜백 페이지
```
min-h-screen flex flex-col items-center justify-center bg-gray-50  // 전체 컨테이너
text-lg font-medium text-gray-900   // 메인 텍스트
text-sm text-gray-500 mt-1          // 보조 텍스트
```

---

## 9. tailwind.config.ts 변경사항

별도 커스텀 색상 추가는 불필요하다. 카카오 색상(`#FEE500`)은 인라인 스타일로 처리하여 카카오 브랜드 가이드라인 준수 여부를 명확히 한다.

---

## 10. 프론트엔드 팀 전달사항

### 신규 파일
| 파일 | 설명 |
|------|------|
| `src/components/common/SocialLoginButton.tsx` | 소셜 로그인 버튼 컴포넌트 |
| `src/app/auth/callback/page.tsx` | OAuth 콜백 처리 페이지 |

### 수정 파일
| 파일 | 변경 내용 |
|------|-----------|
| `src/app/login/page.tsx` | 소셜 버튼 + 구분선 추가, URL error 파라미터 처리 추가 |
| `src/app/signup/page.tsx` | 소셜 버튼(상단) + 구분선 추가 |

### 백엔드 팀 전달사항
아래 API 엔드포인트가 필요하다:
| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/auth/oauth/kakao` | 카카오 OAuth 인증 URL로 302 리다이렉트 |
| GET | `/api/auth/oauth/google` | 구글 OAuth 인증 URL로 302 리다이렉트 |
| POST | `/api/auth/oauth/{provider}` | OAuth 인가 코드를 받아 JWT 토큰 발급 (`{ code: string }`) |

응답 형식은 기존 로그인 API와 동일한 `ApiResponse<LoginResponse>` 래퍼를 사용한다.
