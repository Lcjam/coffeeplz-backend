#!/bin/bash

# CoffeePlz 배포 스크립트
# Usage: ./deploy.sh [환경] [버전]
# 예시: ./deploy.sh prod v1.0.0

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 로그 함수
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 사용법 출력
usage() {
    echo "Usage: $0 [ENVIRONMENT] [VERSION]"
    echo "ENVIRONMENT: dev|test|staging|prod"
    echo "VERSION: Docker image tag (optional, defaults to 'latest')"
    echo ""
    echo "Examples:"
    echo "  $0 dev latest"
    echo "  $0 prod v1.0.0"
    exit 1
}

# 파라미터 검증
ENVIRONMENT=${1:-dev}
VERSION=${2:-latest}

if [[ ! "$ENVIRONMENT" =~ ^(dev|test|staging|prod)$ ]]; then
    log_error "Invalid environment: $ENVIRONMENT"
    usage
fi

log_info "Starting deployment to $ENVIRONMENT environment with version $VERSION"

# 환경별 설정
case $ENVIRONMENT in
    "dev")
        COMPOSE_FILE="docker-compose.dev.yml"
        ENV_FILE=".env.dev"
        ;;
    "test")
        COMPOSE_FILE="docker-compose.test.yml"
        ENV_FILE=".env.test"
        ;;
    "staging")
        COMPOSE_FILE="docker-compose.staging.yml"
        ENV_FILE=".env.staging"
        ;;
    "prod")
        COMPOSE_FILE="docker-compose.prod.yml"
        ENV_FILE=".env.prod"
        ;;
esac

# 환경변수 파일 확인
if [[ ! -f "$ENV_FILE" ]]; then
    log_warning "Environment file $ENV_FILE not found. Using default values."
else
    log_info "Loading environment variables from $ENV_FILE"
    export $(cat $ENV_FILE | xargs)
fi

# Docker Compose 파일 확인
if [[ ! -f "$COMPOSE_FILE" ]]; then
    log_error "Docker Compose file $COMPOSE_FILE not found"
    exit 1
fi

# 이미지 태그 설정
export IMAGE_TAG=$VERSION

# 프로덕션 환경에서는 추가 확인
if [[ "$ENVIRONMENT" == "prod" ]]; then
    log_warning "You are about to deploy to PRODUCTION environment!"
    echo -n "Are you sure you want to continue? (yes/no): "
    read confirm
    if [[ "$confirm" != "yes" ]]; then
        log_info "Deployment cancelled by user"
        exit 0
    fi
fi

# Docker 네트워크 및 볼륨 생성 (존재하지 않는 경우)
log_info "Creating Docker networks and volumes..."
docker network create coffeeplz-${ENVIRONMENT}-network 2>/dev/null || true

# 데이터베이스 백업 (프로덕션 환경)
if [[ "$ENVIRONMENT" == "prod" ]]; then
    log_info "Creating database backup..."
    BACKUP_FILE="backup_$(date +%Y%m%d_%H%M%S).sql"
    docker exec coffeeplz-mysql-prod mysqldump -u root -p${DB_ROOT_PASSWORD} coffeeplz > $BACKUP_FILE
    log_success "Database backup created: $BACKUP_FILE"
fi

# 애플리케이션 헬스체크 함수
health_check() {
    local max_attempts=30
    local attempt=1
    local port=${1:-8080}
    
    log_info "Performing health check..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f http://localhost:$port/actuator/health >/dev/null 2>&1; then
            log_success "Health check passed"
            return 0
        fi
        
        echo -n "."
        sleep 10
        ((attempt++))
    done
    
    log_error "Health check failed after $max_attempts attempts"
    return 1
}

# 롤백 함수
rollback() {
    log_warning "Rolling back to previous version..."
    docker-compose -f $COMPOSE_FILE down
    docker-compose -f $COMPOSE_FILE up -d --remove-orphans
}

# 배포 시작
log_info "Pulling latest images..."
docker-compose -f $COMPOSE_FILE pull

log_info "Stopping existing containers..."
docker-compose -f $COMPOSE_FILE down

log_info "Starting new containers..."
docker-compose -f $COMPOSE_FILE up -d --remove-orphans

# 애플리케이션 시작 대기
log_info "Waiting for application to start..."
sleep 30

# 헬스체크 실행
if [[ "$ENVIRONMENT" == "prod" ]]; then
    PORT=80
else
    PORT=8080
fi

if health_check $PORT; then
    log_success "Deployment to $ENVIRONMENT completed successfully!"
    
    # 배포 후 정리
    log_info "Cleaning up unused Docker images..."
    docker system prune -f
    
else
    log_error "Deployment failed - application is not healthy"
    
    # 롤백 여부 확인
    echo -n "Do you want to rollback? (yes/no): "
    read rollback_confirm
    if [[ "$rollback_confirm" == "yes" ]]; then
        rollback
    fi
    exit 1
fi

# 배포 정보 로깅
log_info "Deployment Summary:"
echo "  Environment: $ENVIRONMENT"
echo "  Version: $VERSION"
echo "  Compose File: $COMPOSE_FILE"
echo "  Timestamp: $(date)"

log_success "Deployment completed successfully!" 