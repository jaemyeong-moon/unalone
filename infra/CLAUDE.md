# 인프라팀 (Infra Team)

## 역할
- VM/K8s 환경에 서비스 배포 및 운영
- 클라우드 환경(AWS, Azure, OCI 등)별 설정 가이드 제공
- 설정값만 입력하면 서비스를 띄울 수 있는 템플릿 관리
- 모니터링(Prometheus, Grafana) 구성

## 환경 구성 파일

### Docker Compose (로컬/VM)
- `docker-compose.yml` - 전체 서비스 오케스트레이션
- `docker-compose.infra.yml` - 인프라 전용

### Kubernetes
```
k8s/
  namespace.yml
  infra/
    kafka.yml, postgresql.yml, mongodb.yml, ingress.yml
  api-service/
    deployment.yml, service.yml
  admin-service/
    deployment.yml, service.yml
  event-service/
    deployment.yml, service.yml
  frontend/
    deployment.yml, service.yml
```

### 모니터링
- `infra/prometheus/prometheus.yml` - 메트릭 스크래핑 설정
- `infra/grafana/provisioning/` - Grafana 데이터소스/대시보드

---

## 환경별 배포 가이드

### 공통 설정값 (환경에 관계없이 필요)

아래 값들만 환경에 맞게 채우면 서비스를 배포할 수 있습니다:

```yaml
# ===== 필수 설정값 =====
infra:
  # PostgreSQL
  POSTGRES_HOST: ""           # DB 호스트 (VM IP, RDS 엔드포인트, etc.)
  POSTGRES_PORT: "5432"
  POSTGRES_DB: "project_db"
  POSTGRES_USER: ""           # DB 사용자
  POSTGRES_PASSWORD: ""       # DB 비밀번호

  # MongoDB
  MONGO_HOST: ""              # MongoDB 호스트
  MONGO_PORT: "27017"
  MONGO_USER: ""
  MONGO_PASSWORD: ""
  MONGO_DATABASE: "project_db"

  # Kafka
  KAFKA_BOOTSTRAP_SERVERS: "" # Kafka 브로커 주소 (host:port)

  # JWT
  JWT_SECRET: ""              # 최소 32바이트 시크릿 키

app:
  # 도메인/네트워크
  DOMAIN: ""                  # 서비스 도메인 (예: unalone.example.com)
  API_SERVICE_URL: ""         # API Service 내부 URL
  ADMIN_SERVICE_URL: ""       # Admin Service 내부 URL
  EVENT_SERVICE_URL: ""       # Event Service 내부 URL

monitoring:
  GRAFANA_ADMIN_USER: "admin"
  GRAFANA_ADMIN_PASSWORD: ""  # Grafana 관리자 비밀번호
```

---

### VM 환경 (Docker Compose)

단일 VM 또는 여러 VM에 Docker Compose로 배포:

```bash
# 1. 환경변수 파일 생성
cp .env.template .env
# .env 파일에 위 설정값 입력

# 2. 전체 서비스 실행
docker compose --env-file .env up -d

# 3. 상태 확인
docker compose ps

# 4. 로그 확인
docker compose logs -f api-service
```

**VM별 참고사항:**
- 방화벽: 3000, 8080-8082, 9090, 3001 포트 오픈 필요
- 메모리: 최소 8GB RAM 권장 (전체 서비스 기준)
- 디스크: PostgreSQL/MongoDB 데이터 볼륨 경로 확인

---

### AWS 환경

| 서비스 | AWS 리소스 | 설정 |
|--------|-----------|------|
| PostgreSQL | **RDS (PostgreSQL 16)** | `POSTGRES_HOST=<rds-endpoint>.rds.amazonaws.com` |
| MongoDB | **DocumentDB** 또는 **EC2 + MongoDB** | `MONGO_HOST=<docdb-endpoint>` |
| Kafka | **MSK (Managed Kafka)** | `KAFKA_BOOTSTRAP_SERVERS=<msk-broker>:9092` |
| Backend | **ECS Fargate** 또는 **EKS** | 각 서비스 Task Definition/Deployment |
| Frontend | **ECS Fargate** 또는 **S3+CloudFront** | Next.js SSR → ECS, Static → S3 |
| Ingress | **ALB (Application Load Balancer)** | 경로 기반 라우팅 |
| 모니터링 | **Amazon Managed Grafana** + **AMP** | 또는 EC2에 self-hosted |

**AWS 추가 설정:**
```yaml
aws:
  REGION: "ap-northeast-2"       # 서울 리전
  VPC_ID: ""
  SUBNET_IDS: ""                 # 프라이빗 서브넷
  SECURITY_GROUP_IDS: ""
  # RDS
  RDS_INSTANCE_CLASS: "db.t3.medium"
  # ECS
  ECS_CLUSTER_NAME: "unalone-cluster"
  ECR_REGISTRY: ""               # ECR 레지스트리 URL
  # ALB
  ALB_CERTIFICATE_ARN: ""        # ACM 인증서 (HTTPS)
```

**접근 권한 (IAM):**
- ECS Task Role: RDS, DocumentDB, MSK, ECR 접근
- CI/CD Role: ECR push, ECS deploy
- 모니터링: CloudWatch, AMP 접근

---

### Azure 환경

| 서비스 | Azure 리소스 | 설정 |
|--------|-------------|------|
| PostgreSQL | **Azure Database for PostgreSQL** | `POSTGRES_HOST=<server>.postgres.database.azure.com` |
| MongoDB | **Cosmos DB (MongoDB API)** | `MONGO_HOST=<account>.mongo.cosmos.azure.com` |
| Kafka | **Azure Event Hubs (Kafka protocol)** | `KAFKA_BOOTSTRAP_SERVERS=<namespace>.servicebus.windows.net:9093` |
| Backend | **AKS** 또는 **Container Apps** | K8s 매니페스트 또는 Container App 설정 |
| Frontend | **AKS** 또는 **Static Web Apps** | SSR → Container, Static → SWA |
| Ingress | **Azure Application Gateway** | 또는 AKS Ingress Controller |
| 모니터링 | **Azure Monitor** + **Managed Grafana** | |

**Azure 추가 설정:**
```yaml
azure:
  SUBSCRIPTION_ID: ""
  RESOURCE_GROUP: "unalone-rg"
  LOCATION: "koreacentral"       # 한국 중부
  # AKS
  AKS_CLUSTER_NAME: "unalone-aks"
  ACR_NAME: ""                   # Azure Container Registry
  # Event Hubs (Kafka 호환)
  EVENTHUB_CONNECTION_STRING: ""
  EVENTHUB_NAMESPACE: ""
```

**접근 권한:**
- Managed Identity: DB, Event Hubs, ACR 접근
- RBAC: AKS 클러스터 관리자, Contributor 역할

---

### OCI (Oracle Cloud) 환경

| 서비스 | OCI 리소스 | 설정 |
|--------|-----------|------|
| PostgreSQL | **OCI Database with PostgreSQL** | `POSTGRES_HOST=<db-endpoint>` |
| MongoDB | **OCI VM + MongoDB** (self-managed) | `MONGO_HOST=<vm-ip>` |
| Kafka | **OCI Streaming (Kafka compatible)** | `KAFKA_BOOTSTRAP_SERVERS=<streaming-endpoint>` |
| Backend | **OKE (Oracle Kubernetes Engine)** | K8s 매니페스트 적용 |
| Frontend | **OKE** 또는 **OCI Object Storage + CDN** | |
| Ingress | **OCI Load Balancer** | OKE Ingress Controller |
| 모니터링 | **OCI Monitoring** + self-hosted Grafana | |

**OCI 추가 설정:**
```yaml
oci:
  TENANCY_OCID: ""
  COMPARTMENT_OCID: ""
  REGION: "ap-seoul-1"           # 서울 리전
  # OKE
  OKE_CLUSTER_ID: ""
  OCIR_ENDPOINT: ""              # OCI Container Registry
  # Streaming
  OCI_STREAM_POOL_ID: ""
  OCI_STREAM_ENDPOINT: ""
```

**접근 권한:**
- IAM Policy: OKE, DB, Streaming, OCIR 접근 정책
- Dynamic Group: 인스턴스/컨테이너 기반 인증

---

## K8s 배포 절차 (공통)

```bash
# 1. 네임스페이스 생성
kubectl apply -f k8s/namespace.yml

# 2. Secret 생성 (설정값 주입)
kubectl create secret generic unalone-secrets \
  --from-literal=POSTGRES_PASSWORD=<value> \
  --from-literal=MONGO_PASSWORD=<value> \
  --from-literal=JWT_SECRET=<value> \
  -n unalone

# 3. 인프라 배포
kubectl apply -f k8s/infra/

# 4. 백엔드 서비스 배포
kubectl apply -f k8s/api-service/
kubectl apply -f k8s/admin-service/
kubectl apply -f k8s/event-service/

# 5. 프론트엔드 배포
kubectl apply -f k8s/frontend/

# 6. 상태 확인
kubectl get pods -n unalone
kubectl get svc -n unalone
```

## 포트 매핑
| 서비스 | 내부 포트 | 외부 노출 |
|--------|----------|----------|
| Frontend | 3000 | 80/443 (Ingress) |
| API Service | 8080 | /api/* (Ingress) |
| Admin Service | 8081 | /admin/* (Ingress) |
| Event Service | 8082 | /events/* (Ingress) |
| PostgreSQL | 5432 | 내부만 |
| MongoDB | 27017 | 내부만 |
| Kafka | 9092 | 내부만 |
| Prometheus | 9090 | 내부/관리자만 |
| Grafana | 3000 | 3001 또는 별도 도메인 |

## 작업 규칙
- 환경별 설정 템플릿: `infra/env-templates/` 디렉토리에 저장
- 파일명: `env-{cloud}-{환경}.yml` (예: `env-aws-prod.yml`)
- 시크릿(비밀번호, 키)은 절대 Git에 커밋하지 않음 → `.gitignore` 필수
- 인프라 변경 시 `docker-compose.yml`, `k8s/` 매니페스트 동시 업데이트
- 새 서비스 추가 시 포트, 네트워크, 의존성 문서 갱신
