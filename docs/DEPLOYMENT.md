# Unalone 프로덕션 배포 가이드

## 개요

단일 VM에 Docker Compose로 전체 서비스를 배포하고, 도메인 연결 + HTTPS(Let's Encrypt) 자동 설정까지 완료하는 가이드입니다.

## 아키텍처

```
인터넷
  │
  ▼
[80/443] Traefik (리버스 프록시 + 자동 SSL)
  │
  ├── /api/*           → api-service:8080
  ├── /api/admin/*     → admin-service:8081
  ├── /grafana/*       → grafana:3000
  └── /*               → frontend:3000

내부 네트워크 (app-network)
  ├── PostgreSQL:5432
  ├── MongoDB:27017
  ├── Kafka:29092
  ├── Zookeeper:2181
  └── Prometheus:9090
```

**외부 노출 포트: 80 (HTTP→HTTPS 리다이렉트), 443 (HTTPS) 만 사용**

---

## 사전 요구사항

| 항목 | 최소 | 권장 |
|------|------|------|
| CPU | 2 Core | 4 Core |
| RAM | 4 GB | 8 GB |
| 디스크 | 20 GB | 50 GB |
| OS | Ubuntu 20.04+ / CentOS 8+ | Ubuntu 22.04 LTS |
| Docker | 24.0+ | 최신 |
| Docker Compose | V2 | 최신 |

**네트워크 요구사항:**
- 도메인 A 레코드가 VM의 공인 IP를 가리키고 있어야 함
- 80, 443 포트가 외부에서 접근 가능해야 함

---

## 배포 절차

### 1단계: 서버 초기 설정

```bash
# 프로젝트 클론
git clone <repository-url> /opt/unalone
cd /opt/unalone

# Docker 설치 및 서버 설정
./scripts/deploy.sh --setup
```

이 명령은:
- Docker / Docker Compose V2 설치
- 방화벽 80/443 포트 오픈
- 시스템 요구사항 검증

### 2단계: 도메인 DNS 설정

도메인 관리 패널에서 A 레코드를 설정합니다:

```
타입    이름                     값              TTL
A       unalone.example.com      123.456.78.90   300
```

> **확인**: `dig +short unalone.example.com` 으로 IP가 올바른지 확인

### 3단계: 환경변수 설정

```bash
./scripts/deploy.sh --init
```

대화형으로 도메인과 이메일을 입력하면:
- `.env` 파일 자동 생성
- DB 비밀번호, JWT Secret 자동 생성 (암호학적 랜덤)
- 생성된 비밀번호를 화면에 출력 → **반드시 별도 보관**

### 4단계: 배포

```bash
./scripts/deploy.sh --deploy
```

이 명령은:
1. `.env` 필수값 검증
2. Docker 이미지 빌드 (병렬)
3. 인프라 서비스 시작 (DB, Kafka)
4. 전체 서비스 시작
5. 헬스 체크
6. 접속 URL 출력

> 최초 빌드 시 10-15분 소요됩니다. SSL 인증서는 최초 HTTPS 접속 시 자동 발급됩니다 (1-2분).

---

## 운영 명령어

```bash
# 서비스 상태 확인
./scripts/deploy.sh --status

# 로그 확인 (전체)
./scripts/deploy.sh --logs

# 특정 서비스 로그
./scripts/deploy.sh --logs api-service

# 코드 업데이트 후 재배포 (인프라는 유지)
git pull
./scripts/deploy.sh --update

# 서비스 중지 (데이터 유지)
./scripts/deploy.sh --stop

# DB 백업
./scripts/deploy.sh --backup
```

---

## 접속 정보

배포 완료 후 아래 URL로 접속합니다:

| 서비스 | URL | 인증 |
|--------|-----|------|
| 웹 서비스 | `https://도메인/` | - |
| API | `https://도메인/api/` | JWT |
| Admin API | `https://도메인/api/admin/` | Basic Auth |
| Grafana | `https://도메인/grafana/` | admin / (생성된 비밀번호) |

---

## 파일 구조

```
docker-compose.prod.yml          # 프로덕션 Docker Compose
infra/
  traefik/
    traefik.prod.yml             # Traefik HTTPS 설정 (Let's Encrypt)
    dynamic.prod.yml             # 라우팅 규칙 + 보안 헤더
  env-templates/
    .env.prod.template           # 환경변수 템플릿
scripts/
  deploy.sh                     # 원커맨드 배포 스크립트
```

---

## 개발 환경 vs 프로덕션 환경

| 항목 | 개발 (docker-compose.yml) | 프로덕션 (docker-compose.prod.yml) |
|------|--------------------------|----------------------------------|
| SSL/HTTPS | X | Let's Encrypt 자동 |
| 포트 노출 | 80, 5432, 27017, 9192, ... | **80, 443만** |
| 비밀번호 | 하드코딩 (postgres/postgres) | .env로 분리 + 자동생성 |
| 재시작 | on-failure | unless-stopped |
| Kafka | 외부 포트 노출 | 내부 통신만 |
| Grafana | 별도 포트 3001 | /grafana 서브패스 |
| Kafka UI | 포함 | 제외 (보안) |
| Traefik 대시보드 | 포트 8090 | 비활성화 |

---

## 트러블슈팅

### SSL 인증서 발급 실패

```bash
# Traefik 로그 확인
docker logs traefik 2>&1 | grep -i acme

# 원인 확인 체크리스트:
# 1. 도메인이 이 서버 IP를 가리키는지 확인
dig +short your-domain.com

# 2. 80 포트가 외부에서 접근 가능한지 확인
curl -I http://your-domain.com

# 3. ACME 인증서 초기화 (문제 시)
docker volume rm $(docker volume ls -q | grep acme)
./scripts/deploy.sh --deploy
```

### 서비스가 시작되지 않음

```bash
# 특정 서비스 로그 확인
docker logs api-service --tail 50

# DB 연결 문제 → 인프라 먼저 확인
docker compose -f docker-compose.prod.yml ps postgresql mongodb kafka

# Kafka 연결 대기 중 → 조금 기다리면 자동 재시작
docker compose -f docker-compose.prod.yml restart api-service
```

### 메모리 부족

```bash
# 메모리 사용량 확인
docker stats --no-stream

# 가장 많이 사용하는 컨테이너 확인 후
# Kafka/Zookeeper 메모리 제한 추가 가능
```

---

## 백업 & 복원

### 자동 백업 설정 (Cron)

```bash
# 매일 새벽 3시 백업
crontab -e
0 3 * * * /opt/unalone/scripts/deploy.sh --backup >> /var/log/unalone-backup.log 2>&1
```

### 복원

```bash
# PostgreSQL 복원
gunzip -c backups/postgres_YYYYMMDD_HHMMSS.sql.gz | docker exec -i postgresql psql -U postgres project_db

# MongoDB 복원
docker exec -i mongodb mongorestore --username=mongo --password=<PASSWORD> --authenticationDatabase=admin --db=project_db --archive --gzip < backups/mongo_YYYYMMDD_HHMMSS.gz
```

---

## 보안 권장사항

1. **SSH 접속 제한**: SSH 키 기반 인증만 허용, 비밀번호 로그인 비활성화
2. **방화벽**: 80/443 외 모든 포트 차단 (SSH 포트는 특정 IP만 허용)
3. **.env 보관**: `.env` 파일은 Git에 커밋하지 않음 (`.gitignore`에 포함)
4. **정기 백업**: 최소 일 1회 DB 백업
5. **모니터링**: Grafana 대시보드로 서비스 상태 상시 모니터링
6. **업데이트**: `docker compose pull`로 베이스 이미지 주기적 업데이트
