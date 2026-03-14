#!/bin/bash
set -euo pipefail

# =============================================
# Unalone 프로덕션 배포 스크립트
# 사용법: ./scripts/deploy.sh [옵션]
#
# 옵션:
#   --setup     최초 서버 설정 (Docker 설치 포함)
#   --init      .env 생성 및 초기 설정
#   --deploy    빌드 및 배포 (기본값)
#   --update    서비스만 재빌드 및 재배포
#   --status    서비스 상태 확인
#   --logs      로그 확인
#   --stop      서비스 중지
#   --backup    DB 백업
# =============================================

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
COMPOSE_FILE="$PROJECT_DIR/docker-compose.prod.yml"
ENV_FILE="$PROJECT_DIR/.env"
BACKUP_DIR="$PROJECT_DIR/backups"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info()  { echo -e "${BLUE}[INFO]${NC} $1"; }
log_ok()    { echo -e "${GREEN}[OK]${NC} $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# =============================================
# 서버 초기 설정 (Docker, Docker Compose 설치)
# =============================================
setup_server() {
    log_info "서버 초기 설정을 시작합니다..."

    # Docker 설치 확인
    if command -v docker &> /dev/null; then
        log_ok "Docker가 이미 설치되어 있습니다: $(docker --version)"
    else
        log_info "Docker를 설치합니다..."
        curl -fsSL https://get.docker.com | sh
        sudo usermod -aG docker "$USER"
        log_ok "Docker 설치 완료. 재로그인 후 docker 명령을 사용할 수 있습니다."
    fi

    # Docker Compose V2 확인
    if docker compose version &> /dev/null; then
        log_ok "Docker Compose V2: $(docker compose version --short)"
    else
        log_error "Docker Compose V2가 필요합니다. Docker Desktop 또는 docker-compose-plugin을 설치하세요."
        exit 1
    fi

    # 방화벽 설정 (ufw)
    if command -v ufw &> /dev/null; then
        log_info "방화벽 포트를 엽니다 (80, 443)..."
        sudo ufw allow 80/tcp
        sudo ufw allow 443/tcp
        sudo ufw --force enable
        log_ok "방화벽 설정 완료 (80, 443 포트 오픈)"
    fi

    # 시스템 요구사항 확인
    TOTAL_MEM=$(free -m | awk '/^Mem:/{print $2}')
    if [ "$TOTAL_MEM" -lt 4096 ]; then
        log_warn "메모리가 ${TOTAL_MEM}MB입니다. 최소 4GB, 권장 8GB 이상입니다."
    else
        log_ok "메모리: ${TOTAL_MEM}MB"
    fi

    DISK_AVAIL=$(df -BG "$PROJECT_DIR" | awk 'NR==2{print $4}' | tr -d 'G')
    if [ "$DISK_AVAIL" -lt 20 ]; then
        log_warn "디스크 여유 공간이 ${DISK_AVAIL}GB입니다. 최소 20GB 권장합니다."
    else
        log_ok "디스크 여유: ${DISK_AVAIL}GB"
    fi

    log_ok "서버 초기 설정 완료!"
}

# =============================================
# .env 파일 생성 (대화형)
# =============================================
init_env() {
    if [ -f "$ENV_FILE" ]; then
        log_warn ".env 파일이 이미 존재합니다."
        read -p "덮어쓰시겠습니까? (y/N): " -r
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            log_info ".env 생성을 건너뜁니다."
            return
        fi
    fi

    log_info ".env 파일을 생성합니다..."
    echo ""

    # 도메인
    read -p "도메인 입력 (예: unalone.example.com): " DOMAIN
    DOMAIN=${DOMAIN:-unalone.example.com}

    # SSL 이메일
    read -p "Let's Encrypt 이메일 (SSL 인증서용): " ACME_EMAIL
    ACME_EMAIL=${ACME_EMAIL:-admin@example.com}

    # 비밀번호 자동 생성
    PG_PASS=$(openssl rand -base64 24 | tr -d '/+=' | head -c 24)
    MONGO_PASS=$(openssl rand -base64 24 | tr -d '/+=' | head -c 24)
    JWT=$(openssl rand -base64 48 | tr -d '/+=' | head -c 48)
    GF_PASS=$(openssl rand -base64 16 | tr -d '/+=' | head -c 16)

    cat > "$ENV_FILE" << EOF
# Unalone Production Environment
# Generated: $(date '+%Y-%m-%d %H:%M:%S')

# ===== Domain & SSL =====
DOMAIN=${DOMAIN}
ACME_EMAIL=${ACME_EMAIL}

# ===== PostgreSQL =====
POSTGRES_DB=project_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=${PG_PASS}

# ===== MongoDB =====
MONGO_USER=mongo
MONGO_PASSWORD=${MONGO_PASS}
MONGO_DATABASE=project_db

# ===== JWT =====
JWT_SECRET=${JWT}

# ===== Grafana =====
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=${GF_PASS}
EOF

    chmod 600 "$ENV_FILE"
    log_ok ".env 파일 생성 완료 (권한: 600)"
    echo ""
    log_info "=== 생성된 접속 정보 ==="
    echo -e "  도메인:         ${GREEN}https://${DOMAIN}${NC}"
    echo -e "  Grafana:        ${GREEN}https://${DOMAIN}/grafana${NC}"
    echo -e "  Grafana 계정:   admin / ${GF_PASS}"
    echo -e "  PostgreSQL PW:  ${PG_PASS}"
    echo -e "  MongoDB PW:     ${MONGO_PASS}"
    echo ""
    log_warn "위 비밀번호를 안전한 곳에 별도 보관하세요!"
}

# =============================================
# 배포 (빌드 + 실행)
# =============================================
deploy() {
    log_info "프로덕션 배포를 시작합니다..."

    # .env 확인
    if [ ! -f "$ENV_FILE" ]; then
        log_error ".env 파일이 없습니다. './scripts/deploy.sh --init' 을 먼저 실행하세요."
        exit 1
    fi

    # .env 필수값 검증
    source "$ENV_FILE"
    local missing=0
    for var in DOMAIN ACME_EMAIL POSTGRES_PASSWORD MONGO_PASSWORD JWT_SECRET GRAFANA_ADMIN_PASSWORD; do
        if [ -z "${!var:-}" ]; then
            log_error ".env에서 ${var} 값이 비어 있습니다."
            missing=1
        fi
    done
    if [ "$missing" -eq 1 ]; then
        exit 1
    fi

    # ACME 디렉토리 준비
    log_info "SSL 인증서 저장소를 준비합니다..."
    docker volume create --name "$(basename "$PROJECT_DIR")_traefik-acme" 2>/dev/null || true

    # 빌드
    log_info "Docker 이미지를 빌드합니다... (최초 실행 시 시간이 걸립니다)"
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" build --parallel

    # 인프라 먼저 시작
    log_info "인프라 서비스를 시작합니다 (DB, Kafka)..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d zookeeper kafka postgresql mongodb

    log_info "인프라 서비스 준비 대기 중..."
    sleep 10

    # 전체 서비스 시작
    log_info "전체 서비스를 시작합니다..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d

    # 헬스체크
    log_info "서비스 상태를 확인합니다..."
    sleep 15
    check_status

    echo ""
    log_ok "========================================="
    log_ok "  배포 완료!"
    log_ok "========================================="
    echo ""
    echo -e "  서비스:     ${GREEN}https://${DOMAIN}${NC}"
    echo -e "  Grafana:    ${GREEN}https://${DOMAIN}/grafana${NC}"
    echo -e "  API:        ${GREEN}https://${DOMAIN}/api${NC}"
    echo -e "  Admin API:  ${GREEN}https://${DOMAIN}/api/admin${NC}"
    echo ""
    log_info "SSL 인증서는 최초 접속 시 자동 발급됩니다 (1-2분 소요)."
    log_info "'./scripts/deploy.sh --logs' 로 로그를 확인하세요."
}

# =============================================
# 서비스만 업데이트 (인프라 유지)
# =============================================
update_services() {
    log_info "서비스를 재빌드 및 재배포합니다..."

    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" build --parallel api-service admin-service event-service frontend
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d --no-deps api-service admin-service event-service frontend

    sleep 10
    check_status
    log_ok "서비스 업데이트 완료!"
}

# =============================================
# 상태 확인
# =============================================
check_status() {
    echo ""
    log_info "=== 서비스 상태 ==="
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"

    echo ""
    log_info "=== 헬스 체크 ==="
    local services=("traefik" "postgresql" "mongodb" "kafka" "api-service" "admin-service" "event-service" "frontend")
    for svc in "${services[@]}"; do
        if docker ps --filter "name=$svc" --filter "status=running" --format "{{.Names}}" | grep -q "$svc"; then
            echo -e "  ${GREEN}[UP]${NC} $svc"
        else
            echo -e "  ${RED}[DOWN]${NC} $svc"
        fi
    done
}

# =============================================
# 로그 확인
# =============================================
show_logs() {
    local service="${2:-}"
    if [ -n "$service" ]; then
        docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" logs -f --tail=100 "$service"
    else
        docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" logs -f --tail=50
    fi
}

# =============================================
# 서비스 중지
# =============================================
stop_services() {
    log_warn "서비스를 중지합니다..."
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" down
    log_ok "서비스 중지 완료 (데이터 볼륨은 유지됩니다)"
}

# =============================================
# DB 백업
# =============================================
backup_db() {
    local timestamp=$(date '+%Y%m%d_%H%M%S')
    mkdir -p "$BACKUP_DIR"

    source "$ENV_FILE"

    log_info "PostgreSQL 백업 중..."
    docker exec postgresql pg_dump -U "${POSTGRES_USER}" "${POSTGRES_DB}" | gzip > "$BACKUP_DIR/postgres_${timestamp}.sql.gz"
    log_ok "PostgreSQL 백업: $BACKUP_DIR/postgres_${timestamp}.sql.gz"

    log_info "MongoDB 백업 중..."
    docker exec mongodb mongodump --username="${MONGO_USER}" --password="${MONGO_PASSWORD}" --authenticationDatabase=admin --db="${MONGO_DATABASE}" --archive --gzip > "$BACKUP_DIR/mongo_${timestamp}.gz"
    log_ok "MongoDB 백업: $BACKUP_DIR/mongo_${timestamp}.gz"

    log_ok "백업 완료!"
}

# =============================================
# 메인
# =============================================
case "${1:-deploy}" in
    --setup)   setup_server ;;
    --init)    init_env ;;
    --deploy)  deploy ;;
    --update)  update_services ;;
    --status)  check_status ;;
    --logs)    show_logs "$@" ;;
    --stop)    stop_services ;;
    --backup)  backup_db ;;
    --help|-h)
        echo "사용법: ./scripts/deploy.sh [옵션]"
        echo ""
        echo "옵션:"
        echo "  --setup     최초 서버 설정 (Docker 설치, 방화벽)"
        echo "  --init      .env 생성 (비밀번호 자동 생성)"
        echo "  --deploy    빌드 및 전체 배포 (기본값)"
        echo "  --update    백엔드/프론트엔드만 재빌드"
        echo "  --status    서비스 상태 확인"
        echo "  --logs      로그 확인 (--logs [서비스명])"
        echo "  --stop      서비스 중지"
        echo "  --backup    DB 백업 (PostgreSQL + MongoDB)"
        echo "  --help      도움말"
        ;;
    *)
        log_error "알 수 없는 옵션: $1"
        echo "도움말: ./scripts/deploy.sh --help"
        exit 1
        ;;
esac
