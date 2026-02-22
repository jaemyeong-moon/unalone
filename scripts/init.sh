#!/bin/bash
set -e

echo "========================================="
echo "  Project Template Initialization"
echo "========================================="

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Check prerequisites
echo -e "\n${YELLOW}[1/5] Checking prerequisites...${NC}"
command -v java >/dev/null 2>&1 || { echo "Java 17+ is required but not installed."; exit 1; }
command -v node >/dev/null 2>&1 || { echo "Node.js 20+ is required but not installed."; exit 1; }
command -v docker >/dev/null 2>&1 || { echo "Docker is required but not installed."; exit 1; }

echo -e "${GREEN}All prerequisites met.${NC}"

# Start infrastructure
echo -e "\n${YELLOW}[2/5] Starting infrastructure (Kafka, PostgreSQL, MongoDB)...${NC}"
docker compose -f docker-compose.infra.yml up -d

# Wait for services
echo -e "\n${YELLOW}[3/5] Waiting for services to be ready...${NC}"
sleep 10

# Build backend
echo -e "\n${YELLOW}[4/5] Building backend services...${NC}"
cd backend && ./gradlew build -x test && cd ..

# Install frontend dependencies
echo -e "\n${YELLOW}[5/5] Installing frontend dependencies...${NC}"
cd frontend && npm install && cd ..

echo -e "\n${GREEN}=========================================${NC}"
echo -e "${GREEN}  Initialization complete!${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""
echo "To start services individually:"
echo "  API Service:    cd backend && ./gradlew :api-service:bootRun"
echo "  Admin Service:  cd backend && ./gradlew :admin-service:bootRun"
echo "  Event Service:  cd backend && ./gradlew :event-service:bootRun"
echo "  Frontend:       cd frontend && npm run dev"
echo ""
echo "Or start everything with Docker:"
echo "  docker compose up -d"
