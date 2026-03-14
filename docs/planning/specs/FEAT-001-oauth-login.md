# FEAT-001: OAuth 소셜 로그인 (카카오 / 구글)

## 목적
현재 Unalone 서비스는 이메일/패스워드 기반 인증만 지원하고 있다. OAuth 소셜 로그인을 도입하여 다음 목표를 달성한다:

1. **가입 허들 감소**: 별도 비밀번호 생성 없이 기존 카카오/구글 계정으로 즉시 가입 가능
2. **보안 강화**: 패스워드를 직접 저장하지 않아 유출 위험 제거, OAuth 제공자의 2FA 등 보안 기능 활용
3. **사용자 편의**: 비밀번호 분실 문제 해소, 원클릭 로그인으로 재방문율 향상
4. **MVP 요구사항 충족**: IDEA_TEMPLATE에 정의된 핵심 기능 중 OAuth 로그인 항목 구현

## 대상 사용자
- **일반 사용자 (ROLE_USER)**: 소셜 계정으로 회원가입/로그인하는 1인 가구 사용자

## 선행 조건
- 카카오 개발자 콘솔에서 애플리케이션 등록 및 Redirect URI 설정 완료
- Google Cloud Console에서 OAuth 2.0 클라이언트 ID 발급 및 Redirect URI 설정 완료
- 백엔드 환경 변수에 각 제공자의 Client ID / Client Secret 등록

## OAuth 제공자
| 제공자 | 식별자 | 취득 정보 | 비고 |
|--------|--------|-----------|------|
| 카카오 | `kakao` | 이메일, 닉네임, 프로필 이미지 | 국내 사용자 주력 |
| 구글 | `google` | 이메일, 이름, 프로필 이미지 | 글로벌 호환성 |

---

## 사용자 스토리

### US-001: 소셜 로그인으로 신규 가입
```
AS A 1인 가구 사용자
I WANT TO 카카오 또는 구글 계정으로 간편하게 회원가입하고 싶다
SO THAT 별도 비밀번호 없이 빠르게 서비스를 이용할 수 있다
```

### US-002: 소셜 로그인으로 재로그인
```
AS A 기존 소셜 가입 사용자
I WANT TO 소셜 계정 버튼 하나로 로그인하고 싶다
SO THAT 비밀번호를 기억할 필요 없이 편리하게 접속할 수 있다
```

### US-003: 기존 이메일 계정에 소셜 연동
```
AS A 이메일/패스워드로 가입한 기존 사용자
I WANT TO 내 계정에 카카오/구글 계정을 연동하고 싶다
SO THAT 다음부터 소셜 로그인으로도 접속할 수 있다
```

### US-004: 소셜 연동 해제
```
AS A 소셜 계정이 연동된 사용자
I WANT TO 특정 소셜 연동을 해제하고 싶다
SO THAT 더 이상 해당 소셜 계정으로 로그인되지 않도록 관리할 수 있다
```

---

## 기능 상세

### 1. 주요 흐름 (Happy Path)

#### 1-1. 소셜 로그인 (신규 사용자 - 자동 회원가입)
```
1. 사용자가 로그인 페이지에서 "카카오로 로그인" 또는 "구글로 로그인" 버튼 클릭
2. 프론트엔드가 GET /api/auth/oauth/{provider} 호출
3. 백엔드가 해당 OAuth 제공자의 인증 URL을 생성하여 redirect URL 응답
4. 프론트엔드가 OAuth 제공자 페이지로 리다이렉트
5. 사용자가 OAuth 제공자에서 로그인 및 정보 제공 동의
6. OAuth 제공자가 Authorization Code와 함께 콜백 URL로 리다이렉트
7. GET /api/auth/oauth/{provider}/callback?code={code} 호출
8. 백엔드가 Authorization Code로 Access Token 교환
9. Access Token으로 사용자 정보(이메일, 이름) 조회
10. 해당 OAuth 제공자+ID로 기존 사용자 조회 -> 없으면 자동 회원가입
    - User 엔티티 생성 (oauthProvider, oauthId 설정, password는 null 허용)
    - 기본 Profile 생성 (checkIntervalHours: 24, activeHours: 08:00~22:00)
11. JWT 토큰 발급
12. 프론트엔드로 JWT 토큰과 함께 리다이렉트 (redirect URL에 토큰 포함)
13. 프론트엔드가 토큰 저장 후 대시보드(/)로 이동
```

#### 1-2. 소셜 로그인 (기존 사용자 - 재로그인)
```
1~9. 상동
10. 해당 OAuth 제공자+ID로 기존 사용자 조회 -> 있으면 바로 JWT 발급
11. 프론트엔드로 JWT 토큰과 함께 리다이렉트
12. 프론트엔드가 토큰 저장 후 대시보드(/)로 이동
```

#### 1-3. 기존 이메일 계정에 소셜 연동
```
1. 로그인 상태의 사용자가 프로필/설정 페이지에서 "카카오 연동" 버튼 클릭
2. OAuth 인증 흐름 수행 (위 1~9 과정)
3. POST /api/auth/oauth/link (JWT + OAuth 정보) 호출
4. 현재 로그인된 사용자에 oauthProvider, oauthId 연동 저장
5. 연동 완료 응답
```

### 2. 예외 흐름 (Edge Cases)

#### 2-1. 이미 가입된 이메일로 소셜 로그인 시도
```
- 조건: OAuth에서 받은 이메일이 이미 이메일/패스워드로 가입된 계정에 존재
- 처리: 자동 연동하지 않음. "이미 해당 이메일로 가입된 계정이 있습니다.
         기존 계정에 로그인 후 소셜 계정을 연동해주세요." 안내 메시지 표시
- 사유: 보안상 이메일만으로 자동 연동하면 타인의 계정 탈취 위험
```

#### 2-2. OAuth 인증 실패 / 사용자 취소
```
- 조건: OAuth 제공자에서 인증 실패 또는 사용자가 동의를 취소
- 처리: 에러 코드와 함께 로그인 페이지로 리다이렉트
- 메시지: "소셜 로그인이 취소되었습니다. 다시 시도해주세요."
```

#### 2-3. OAuth 제공자 서버 장애
```
- 조건: 카카오/구글 서버가 응답하지 않거나 타임아웃
- 처리: 5초 타임아웃 설정, 실패 시 로그인 페이지로 리다이렉트
- 메시지: "소셜 로그인 서비스에 일시적인 문제가 있습니다. 잠시 후 다시 시도해주세요."
```

#### 2-4. 이미 다른 계정에 연동된 소셜 계정으로 연동 시도
```
- 조건: 계정 A에 연동된 카카오 계정을 계정 B에 다시 연동 시도
- 처리: 409 Conflict 응답
- 메시지: "이 소셜 계정은 이미 다른 계정에 연동되어 있습니다."
```

#### 2-5. 소셜 연동 해제 시 로그인 수단 부재
```
- 조건: 소셜 로그인으로만 가입한 사용자가 유일한 소셜 연동을 해제 시도
- 처리: 해제 차단
- 메시지: "최소 하나의 로그인 수단이 필요합니다. 비밀번호를 먼저 설정해주세요."
```

### 3. 비즈니스 규칙

| 규칙 ID | 규칙 | 설명 |
|---------|------|------|
| BR-001 | 첫 소셜 로그인 시 자동 회원가입 | OAuth 정보로 User + Profile 자동 생성 |
| BR-002 | 이메일 중복 시 자동 연동 금지 | 보안을 위해 수동 연동만 허용 |
| BR-003 | 소셜 계정은 하나의 Unalone 계정에만 연동 | 1 OAuth = 1 User 관계 |
| BR-004 | 자동 가입 사용자 역할은 ROLE_USER | 관리자는 별도 지정 |
| BR-005 | 소셜 가입 사용자의 password 필드는 null | 패스워드 로그인 불가, 소셜로만 로그인 |
| BR-006 | 최소 1개 로그인 수단 유지 | 패스워드 또는 소셜 연동 중 최소 1개 |
| BR-007 | OAuth state 파라미터로 CSRF 방지 | 인증 요청 시 랜덤 state 생성 및 검증 |

---

## API 엔드포인트 명세

### GET /api/auth/oauth/{provider}
OAuth 로그인 시작 - 제공자 인증 페이지 URL 반환

| 항목 | 내용 |
|------|------|
| **인증** | 불필요 |
| **Path Parameter** | `provider`: `kakao` 또는 `google` |
| **Query Parameter** | `redirect_uri` (optional): 로그인 완료 후 프론트엔드 리다이렉트 URL |

**Response 200:**
```json
{
  "status": "OK",
  "message": "OAuth 인증 URL 생성 성공",
  "data": {
    "authorizationUrl": "https://kauth.kakao.com/oauth/authorize?client_id=...&redirect_uri=...&response_type=code&state=..."
  }
}
```

**Response 400:**
```json
{
  "status": "BAD_REQUEST",
  "message": "지원하지 않는 OAuth 제공자입니다: {provider}"
}
```

---

### GET /api/auth/oauth/{provider}/callback
OAuth 콜백 처리 - Authorization Code를 JWT로 교환

| 항목 | 내용 |
|------|------|
| **인증** | 불필요 |
| **Path Parameter** | `provider`: `kakao` 또는 `google` |
| **Query Parameter** | `code`: Authorization Code, `state`: CSRF 방지 토큰 |

**Response 302 (Redirect):**
프론트엔드 URL로 리다이렉트하며 JWT 토큰을 쿼리 파라미터로 전달
```
Location: http://localhost:3000/oauth/callback?token={jwt_token}&isNewUser={true|false}
```

**Response 400 (state 불일치):**
```json
{
  "status": "BAD_REQUEST",
  "message": "유효하지 않은 인증 요청입니다"
}
```

**Response 409 (이메일 중복):**
프론트엔드 URL로 리다이렉트하며 에러 정보 전달
```
Location: http://localhost:3000/login?error=email_exists&email={email}
```

---

### POST /api/auth/oauth/link
기존 계정에 소셜 계정 연동

| 항목 | 내용 |
|------|------|
| **인증** | JWT 필요 (ROLE_USER) |
| **Content-Type** | application/json |

**Request Body:**
```json
{
  "provider": "kakao",
  "authorizationCode": "authorization_code_from_oauth",
  "state": "csrf_state_token"
}
```

**Response 200:**
```json
{
  "status": "OK",
  "message": "소셜 계정 연동 성공",
  "data": {
    "provider": "kakao",
    "linkedAt": "2026-03-14T10:30:00"
  }
}
```

**Response 409:**
```json
{
  "status": "CONFLICT",
  "message": "이 소셜 계정은 이미 다른 계정에 연동되어 있습니다"
}
```

---

### DELETE /api/auth/oauth/link/{provider}
소셜 계정 연동 해제

| 항목 | 내용 |
|------|------|
| **인증** | JWT 필요 (ROLE_USER) |
| **Path Parameter** | `provider`: `kakao` 또는 `google` |

**Response 200:**
```json
{
  "status": "OK",
  "message": "소셜 계정 연동이 해제되었습니다"
}
```

**Response 400:**
```json
{
  "status": "BAD_REQUEST",
  "message": "최소 하나의 로그인 수단이 필요합니다"
}
```

---

## 데이터 모델 변경

### User 엔티티 변경사항
현재 User 엔티티에 OAuth 관련 필드가 없으므로 별도 테이블로 분리하여 다중 소셜 연동을 지원한다.

#### 신규 테이블: user_oauth_connections
```sql
CREATE TABLE user_oauth_connections (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    oauth_provider VARCHAR(20) NOT NULL,   -- 'kakao', 'google'
    oauth_id VARCHAR(100) NOT NULL,         -- OAuth 제공자의 사용자 고유 ID
    oauth_email VARCHAR(255),               -- OAuth에서 받은 이메일
    connected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE (oauth_provider, oauth_id)       -- 같은 소셜 계정은 하나의 User에만
);
```

#### User 엔티티 변경
- `password` 컬럼: `nullable = true`로 변경 (소셜 전용 사용자는 패스워드 없음)

---

## 수용 기준 (Acceptance Criteria)

### 소셜 로그인 기본 흐름
- [ ] 로그인 페이지에 "카카오로 로그인", "구글로 로그인" 버튼이 표시된다
- [ ] 카카오 로그인 버튼 클릭 시 카카오 OAuth 인증 페이지로 이동한다
- [ ] 구글 로그인 버튼 클릭 시 구글 OAuth 인증 페이지로 이동한다
- [ ] OAuth 인증 완료 후 JWT 토큰이 발급되고 대시보드로 이동한다
- [ ] 최초 소셜 로그인 시 자동으로 회원가입되고 기본 프로필이 생성된다
- [ ] 기존 소셜 가입 사용자가 재로그인 시 정상적으로 JWT가 발급된다

### 계정 연동
- [ ] 이메일/패스워드 계정에 로그인한 상태에서 소셜 계정 연동이 가능하다
- [ ] 연동 후 소셜 로그인으로도 동일 계정에 접속할 수 있다
- [ ] 이미 다른 계정에 연동된 소셜 계정은 중복 연동이 불가하다

### 예외 처리
- [ ] OAuth에서 받은 이메일이 기존 계정과 중복될 경우 안내 메시지가 표시된다
- [ ] OAuth 인증 취소 시 로그인 페이지로 복귀하며 에러 메시지가 표시된다
- [ ] 유효하지 않은 state 파라미터로 콜백 시 요청이 거부된다
- [ ] 지원하지 않는 provider 요청 시 400 에러가 반환된다

### 연동 해제
- [ ] 프로필/설정 페이지에서 소셜 연동 해제가 가능하다
- [ ] 유일한 로그인 수단인 소셜 연동은 해제가 차단된다

### 보안
- [ ] CSRF 방지를 위한 state 파라미터가 생성 및 검증된다
- [ ] OAuth Client Secret이 서버 사이드에서만 사용된다 (프론트엔드 노출 없음)
- [ ] 콜백 URL이 화이트리스트에 등록된 도메인만 허용된다

### 기존 기능 호환성
- [ ] 기존 이메일/패스워드 로그인이 정상 작동한다
- [ ] 기존 회원가입 기능이 정상 작동한다
- [ ] 소셜 로그인 사용자도 체크인, 커뮤니티 등 모든 기능을 이용할 수 있다

---

## 우선순위
**P0 (필수)** - IDEA_TEMPLATE에서 MVP 핵심 기능으로 정의됨. 도메인 모델(User)에 oauthProvider, oauthId가 설계되어 있으나 현재 코드에 미구현 상태.

---

## 디자인팀 인터페이스 (Design Team)

### 필요한 UI 변경사항

#### 1. 로그인 페이지 (`/login`)
- 기존 이메일/패스워드 폼 하단에 구분선 ("또는") 추가
- 카카오 로그인 버튼: 카카오 브랜드 가이드라인 준수 (노란색 배경 #FEE500, 검정 텍스트)
- 구글 로그인 버튼: 구글 브랜드 가이드라인 준수 (흰색 배경, 구글 로고)
- 버튼 순서: 카카오 > 구글 (국내 서비스 우선)

#### 2. 회원가입 페이지 (`/signup`)
- 로그인 페이지와 동일한 소셜 로그인 버튼 추가
- "소셜 계정으로 간편 가입" 문구 표시

#### 3. OAuth 콜백 페이지 (`/oauth/callback`)
- 신규: 로딩 스피너와 "로그인 처리 중..." 문구 표시
- JWT 수신 후 대시보드로 자동 이동
- 에러 시 적절한 에러 메시지와 로그인 페이지 링크 표시

#### 4. 프로필/설정 페이지
- "소셜 계정 연동" 섹션 추가
- 각 제공자별 연동 상태 표시 (연동됨/미연동)
- 연동/해제 버튼

#### 5. 에러 상태 화면
- 이메일 중복 시 계정 연동 안내 화면
- OAuth 실패 시 재시도 안내 화면

### 디자인팀 산출물 요청
- 로그인/회원가입 페이지 소셜 버튼 레이아웃
- OAuth 콜백 로딩 화면
- 소셜 연동 관리 UI (프로필 페이지 내)
- 에러/안내 메시지 디자인

---

## 개발팀 인터페이스 (Development Team)

### Backend 변경사항

#### 1. 의존성 추가 (api-service)
- `spring-boot-starter-oauth2-client` 또는 직접 REST 클라이언트 방식 선택
- 권장: 직접 REST 클라이언트 (WebClient) 사용 - 기존 JWT 발급 로직과 통합 용이

#### 2. 신규 클래스
| 패키지 | 클래스 | 역할 |
|--------|--------|------|
| `domain` | `UserOAuthConnection` | OAuth 연동 엔티티 (@Entity) |
| `repository` | `UserOAuthConnectionRepository` | OAuth 연동 JPA Repository |
| `service` | `OAuthService` | OAuth 인증 흐름 처리 (제공자별 분기) |
| `service` | `KakaoOAuthClient` | 카카오 API 호출 (토큰 교환, 사용자 정보) |
| `service` | `GoogleOAuthClient` | 구글 API 호출 (토큰 교환, 사용자 정보) |
| `dto/auth` | `OAuthLoginResponse` | OAuth 로그인 응답 DTO |
| `dto/auth` | `OAuthLinkRequest` | 계정 연동 요청 DTO |
| `controller` | `OAuthController` | OAuth 관련 엔드포인트 (또는 AuthController 확장) |

#### 3. 기존 코드 변경
| 파일 | 변경 내용 |
|------|----------|
| `User.java` | `password` 필드 nullable 허용 |
| `SecurityConfig.java` | OAuth 콜백 URL `/api/auth/oauth/*/callback` permitAll 추가 |
| `AuthService.java` | 기존 로직 유지, OAuthService와 분리 |
| `application.yml` | OAuth 제공자별 client-id, client-secret, redirect-uri 설정 추가 |

#### 4. 환경 설정 (application.yml)
```yaml
oauth:
  kakao:
    client-id: ${KAKAO_CLIENT_ID}
    client-secret: ${KAKAO_CLIENT_SECRET}
    redirect-uri: ${KAKAO_REDIRECT_URI:http://localhost:8080/api/auth/oauth/kakao/callback}
    token-uri: https://kauth.kakao.com/oauth/token
    user-info-uri: https://kapi.kakao.com/v2/user/me
  google:
    client-id: ${GOOGLE_CLIENT_ID}
    client-secret: ${GOOGLE_CLIENT_SECRET}
    redirect-uri: ${GOOGLE_REDIRECT_URI:http://localhost:8080/api/auth/oauth/google/callback}
    token-uri: https://oauth2.googleapis.com/token
    user-info-uri: https://www.googleapis.com/oauth2/v2/userinfo
```

### Frontend 변경사항

#### 1. 신규 페이지/컴포넌트
| 경로/이름 | 유형 | 역할 |
|-----------|------|------|
| `/oauth/callback/page.tsx` | Page | OAuth 콜백 처리 (토큰 수신, 저장, 리다이렉트) |
| `SocialLoginButtons.tsx` | Component | 카카오/구글 로그인 버튼 공통 컴포넌트 |
| `SocialConnectionManager.tsx` | Component | 소셜 연동 관리 (프로필 페이지 내) |

#### 2. 기존 코드 변경
| 파일 | 변경 내용 |
|------|----------|
| `login/page.tsx` | SocialLoginButtons 컴포넌트 추가 |
| `signup/page.tsx` | SocialLoginButtons 컴포넌트 추가 |
| `profile/page.tsx` | SocialConnectionManager 컴포넌트 추가 |
| `src/types/index.ts` | OAuth 관련 타입 정의 추가 |
| `src/lib/api.ts` | OAuth API 호출 함수 추가 |

---

## 구현 순서 (권장)

| 단계 | 작업 | 담당 |
|------|------|------|
| 1 | DB 마이그레이션 (user_oauth_connections 테이블, password nullable) | Backend |
| 2 | OAuth 엔티티/리포지토리 생성 | Backend |
| 3 | 카카오 OAuth 클라이언트 구현 | Backend |
| 4 | 구글 OAuth 클라이언트 구현 | Backend |
| 5 | OAuthService + OAuthController 구현 | Backend |
| 6 | 소셜 로그인 버튼 디자인 | Design |
| 7 | 프론트엔드 소셜 로그인 버튼 + 콜백 페이지 | Frontend |
| 8 | 계정 연동/해제 API + UI | Backend + Frontend |
| 9 | 통합 테스트 | QA |

---

## 참고 자료
- [카카오 로그인 가이드](https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api)
- [구글 OAuth 2.0 가이드](https://developers.google.com/identity/protocols/oauth2)
- 현재 인증 코드: `backend/api-service/src/main/java/com/project/api/` 하위 `controller/AuthController.java`, `service/AuthService.java`, `security/JwtTokenProvider.java`, `config/SecurityConfig.java`
