# Backend - Spring Boot 마이크로서비스

## 구조
- **common/**: 공통 모듈 (DTO, Event 클래스, Kafka Config) - 모든 서비스가 의존
- **api-service/**: 사용자 API (JWT 인증) - Auth, CheckIn, Community, Guardian, Profile
- **admin-service/**: 관리자 API (HTTP Basic Auth) - Dashboard, User, Alert, Product, Order 관리
- **event-service/**: 이벤트 처리 - Kafka Consumer, MongoDB 로깅, Alert 생성

## 패키지 구조 (각 서비스 공통)
```
com.project.{service}/
  config/        # Security, Bean 설정
  controller/    # REST API 컨트롤러
  domain/        # JPA Entity / MongoDB Document
  dto/           # Request/Response DTO
  exception/     # 비즈니스 예외
  kafka/         # Kafka Producer/Consumer
  repository/    # JPA/MongoDB Repository
  service/       # 비즈니스 로직
  security/      # JWT 관련 (api-service만)
```

## 코딩 컨벤션
- Java 17 문법 사용 (records, sealed classes, pattern matching 등)
- Lombok 사용: `@Getter`, `@Builder`, `@RequiredArgsConstructor` 등
- 엔티티: `@Entity` + `@Builder` 패턴, ID는 `@GeneratedValue(strategy = IDENTITY)`
- DTO: Request/Response 분리, `record` 타입 선호
- 서비스: `@Service` + `@Transactional` + 생성자 주입 (`@RequiredArgsConstructor`)
- 예외: `BusinessException` 상속, `GlobalExceptionHandler`에서 일괄 처리
- API 응답: `ApiResponse<T>` 래퍼로 통일

## Kafka 이벤트
- 토픽: `checkin-events`, `alert-events`
- 이벤트 클래스: `common` 모듈의 `event` 패키지에 정의
- Producer: `EventPublisher` 인터페이스 사용
- Consumer: `@KafkaListener` 어노테이션 사용

## 빌드
```bash
# 전체 빌드
./gradlew build

# 특정 서비스 실행
./gradlew :api-service:bootRun
./gradlew :admin-service:bootRun
./gradlew :event-service:bootRun

# 테스트
./gradlew test
./gradlew :api-service:test
```

## DB 연결
- PostgreSQL: api-service, admin-service (JPA, spring.datasource)
- MongoDB: admin-service, event-service (spring.data.mongodb)
- 설정 파일: 각 서비스의 `src/main/resources/application.yml`

## 주의사항
- common 모듈 변경 시 모든 서비스에 영향 → 하위 호환성 유지
- Kafka 이벤트 스키마 변경 시 Producer/Consumer 양쪽 동시 수정
- Product 엔티티는 낙관적 잠금(`@Version`) 사용 → 동시성 처리 주의
