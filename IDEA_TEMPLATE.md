# 프로젝트 아이디어 입력 템플릿

새로운 프로젝트 아이디어가 있을 때 아래 템플릿을 채워 주세요. 이 템플릿을 기반으로 마이크로서비스 프로젝트 구조가 자동으로 구성됩니다.

---

## 1. 프로젝트 이름

> 프로젝트의 이름을 작성합니다.

**예시:** 온라인 주문 시스템

---

## 2. 프로젝트 설명 (한 줄 요약)

> 프로젝트가 무엇을 하는지 한 문장으로 설명합니다.

**예시:** 사용자가 상품을 검색하고 주문하며, 관리자가 주문을 관리하고 배송 상태를 추적하는 온라인 커머스 시스템

---

## 3. 핵심 기능 목록

> 프로젝트의 핵심 기능을 체크리스트 형태로 나열합니다.

**예시:**

- [ ] 회원 가입 및 로그인 (JWT 인증)
- [ ] 상품 목록 조회 및 상세 보기
- [ ] 장바구니 관리 (추가, 수정, 삭제)
- [ ] 주문 생성 및 결제 처리
- [ ] 주문 상태 실시간 알림
- [ ] 관리자 상품 등록/수정/삭제
- [ ] 관리자 주문 관리 (상태 변경, 취소 처리)
- [ ] 배송 상태 추적
- [ ] 매출 통계 대시보드

---

## 4. 도메인 모델 정의

> 핵심 엔티티와 엔티티 간의 관계를 정의합니다.

**예시:**

| 엔티티 | 주요 필드 | 저장소 | 설명 |
|--------|----------|--------|------|
| User | id, email, password, name, role | PostgreSQL | 회원 정보 |
| Product | id, name, price, stock, category | PostgreSQL | 상품 정보 |
| Order | id, userId, status, totalAmount, createdAt | PostgreSQL | 주문 정보 |
| OrderItem | id, orderId, productId, quantity, price | PostgreSQL | 주문 항목 |
| Cart | id, userId, items[] | MongoDB | 장바구니 (유연한 스키마) |
| Notification | id, userId, type, message, readAt | MongoDB | 알림 이력 |
| OrderEvent | id, orderId, eventType, payload, timestamp | MongoDB | 이벤트 이력 |

**엔티티 관계:**

```
User (1) ──── (N) Order
Order (1) ──── (N) OrderItem
Product (1) ──── (N) OrderItem
User (1) ──── (1) Cart
Cart (1) ──── (N) CartItem (embedded)
```

---

## 5. API 엔드포인트 목록

> 각 서비스별로 제공할 API 엔드포인트를 정의합니다.

### API Service (Port 8080)

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| POST | /api/auth/signup | 회원 가입 | X |
| POST | /api/auth/login | 로그인 | X |
| GET | /api/products | 상품 목록 조회 | X |
| GET | /api/products/{id} | 상품 상세 조회 | X |
| GET | /api/cart | 장바구니 조회 | O |
| POST | /api/cart/items | 장바구니 상품 추가 | O |
| DELETE | /api/cart/items/{id} | 장바구니 상품 삭제 | O |
| POST | /api/orders | 주문 생성 | O |
| GET | /api/orders | 내 주문 목록 조회 | O |
| GET | /api/orders/{id} | 주문 상세 조회 | O |

### Admin Service (Port 8081)

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| GET | /admin/orders | 전체 주문 목록 | O (ADMIN) |
| PATCH | /admin/orders/{id}/status | 주문 상태 변경 | O (ADMIN) |
| POST | /admin/products | 상품 등록 | O (ADMIN) |
| PUT | /admin/products/{id} | 상품 수정 | O (ADMIN) |
| DELETE | /admin/products/{id} | 상품 삭제 | O (ADMIN) |
| GET | /admin/stats/sales | 매출 통계 | O (ADMIN) |
| GET | /admin/users | 회원 목록 | O (ADMIN) |

### Event Service (Port 8082)

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| GET | /events/orders/{orderId} | 주문 이벤트 이력 조회 | O |
| GET | /events/health | 이벤트 서비스 헬스 체크 | X |

---

## 6. 이벤트 정의

> Kafka를 통해 주고받을 이벤트를 정의합니다.

| 이벤트 이름 | 토픽 | 발행 서비스 | 구독 서비스 | 페이로드 |
|------------|------|-----------|-----------|---------|
| OrderCreated | order-events | API Service | Event Service, Admin Service | `{ orderId, userId, items[], totalAmount, createdAt }` |
| OrderStatusChanged | order-events | Admin Service | Event Service, API Service | `{ orderId, previousStatus, newStatus, changedBy, changedAt }` |
| PaymentCompleted | payment-events | API Service | Event Service, Admin Service | `{ orderId, paymentId, amount, method, completedAt }` |
| StockUpdated | product-events | Admin Service | API Service, Event Service | `{ productId, previousStock, newStock, reason }` |
| NotificationRequested | notification-events | Event Service | Event Service | `{ userId, type, title, message, metadata }` |

---

## 7. 관리자 기능 목록

> 관리자 서비스에서 제공할 기능을 상세히 나열합니다.

**예시:**

- [ ] **주문 관리**: 주문 목록 조회, 상태 변경 (접수 → 처리중 → 배송중 → 완료), 주문 취소
- [ ] **상품 관리**: 상품 CRUD, 재고 수량 조정, 카테고리 관리
- [ ] **회원 관리**: 회원 목록 조회, 회원 상태 변경 (활성/비활성), 역할 변경
- [ ] **통계 대시보드**: 일별/월별 매출, 인기 상품 TOP 10, 신규 회원 추이
- [ ] **이벤트 모니터링**: 이벤트 처리 현황, 실패 이벤트 재처리

---

## 8. 비즈니스 규칙

> 시스템이 지켜야 할 핵심 비즈니스 규칙을 나열합니다.

**예시:**

1. 주문 생성 시 재고가 부족하면 주문이 실패해야 한다.
2. 주문 생성과 동시에 재고가 차감되어야 한다 (재고 선점).
3. 결제 실패 시 차감된 재고가 복구되어야 한다.
4. 주문 취소는 '배송중' 상태 이전까지만 가능하다.
5. 동일 상품에 대한 동시 주문 시 재고 정합성이 보장되어야 한다.
6. 관리자만 상품 가격 및 재고를 변경할 수 있다.
7. 주문 상태 변경 시 사용자에게 실시간 알림이 발송되어야 한다.

---

## 9. 비기능 요구사항

> 성능, 보안, 가용성 등 비기능 요구사항을 정의합니다.

**예시:**

### 성능
- API 응답 시간: 95 백분위 기준 500ms 이내
- 동시 사용자: 최소 1,000명 동시 접속 지원
- 이벤트 처리: 초당 최소 500건 이벤트 처리

### 보안
- JWT 기반 인증/인가
- 비밀번호 BCrypt 해싱
- API Rate Limiting (IP당 분당 100회)
- CORS 설정 (허용된 도메인만)
- SQL Injection / XSS 방지

### 가용성
- 서비스 가용성: 99.9%
- 무중단 배포 (Rolling Update)
- Kafka 이벤트 최소 1회 전달 보장 (at-least-once)

### 관측 가능성
- Prometheus 메트릭 수집
- Grafana 대시보드
- 구조화된 로그 (JSON 형식)
- 분산 추적 (선택사항)

---

## 작성 가이드

1. **프로젝트 이름**과 **한 줄 설명**부터 채워 주세요.
2. **핵심 기능 목록**은 MVP 기준으로 우선순위가 높은 것부터 나열합니다.
3. **도메인 모델**은 PostgreSQL과 MongoDB 중 적합한 저장소를 선택합니다.
   - 정형 데이터, 트랜잭션 필요 → PostgreSQL
   - 비정형 데이터, 유연한 스키마 → MongoDB
4. **이벤트 정의**는 서비스 간 비동기 통신이 필요한 부분을 식별합니다.
5. **비즈니스 규칙**은 데이터 정합성과 관련된 규칙을 빠짐없이 기록합니다.
6. 예시 항목(`온라인 주문 시스템`)은 삭제하고 본인의 프로젝트 내용으로 대체합니다.
