#!/bin/bash
set -eu -o pipefail

function success() {
  COLOR='\033[0;32m'
  NC='\033[0m'

  if [[ $2 = "time" ]]; then
    echo -e "${COLOR}[$(date +"%Y-%m-%d %H:%M:%S")] $1${NC}"
  else
    echo -e "${COLOR}$1${NC}"
  fi
}

function warn() {
  COLOR='\033[0;33m'
  NC='\033[0m'

  if [[ $2 = "time" ]]; then
    echo -e "${COLOR}[$(date +"%Y-%m-%d %H:%M:%S")] $1${NC}"
  else
    echo -e "${COLOR}$1${NC}"
  fi
}

function error() {
  COLOR='\033[0;31m'
  NC='\033[0m'

  if [[ $2 = "time" ]]; then
    echo -e "${COLOR}[$(date +"%Y-%m-%d %H:%M:%S")] $1${NC}"
  else
    echo -e "${COLOR}$1${NC}"
  fi
}

function help() {
  success "tools for balance service" "none"
  success "比如: ./ops.sh -bi" "none"
  success "-bi | --build-image: build docker image" "none"
  success "-sl | --start-local: start local environment using docker-compose" "none"
  success "-stl | --stop-local: stop local environment using docker-compose" "none"
  success "-td | --tear-down: cleanup the entire database" "none"
  success "-mu | --mock-up: populate sample data" "none"
  success "-pt | --performance-test: run performance test using k6" "none"
  exit 1
}

if [ $# -eq 0 ]; then
  help
fi

# [start:解析参数，默认值]
action=""

for i in "$@"; do
  case $i in
  -bi* | --build-image*)
    action="build_image"
    shift # past argument=value
    ;;
  -sl* | --start-local*)
    action="start_local"
    shift # past argument=value
    ;;
  -stl* | --stop-local*)
    action="stop_local"
    shift # past argument=value
    ;;
  -td* | --tear-down*)
    action="tear_down"
    shift # past argument=value
    ;;
  -mu* | --mock-up*)
    action="mock_up"
    shift # past argument=value
    ;;
  -pt* | --performance-test*)
    action="performance_test"
    shift # past argument=value
    ;;
  -* | --*)
    error "Unknown option $i" "time"
    exit 1
    ;;
  *) ;;
  esac
done

# Execute actions based on parameters
if [ "$action" = "build_image" ]; then
  success "Building Docker image..." "time"
  docker build -t balance-service .
  success "Docker image built successfully" "time"
elif [ "$action" = "start_local" ]; then
  success "Starting local environment..." "time"
  
  # Check if docker-compose.yaml exists
  if [ ! -f "docker-compose.yaml" ]; then
    error "docker-compose.yaml not found" "time"
    exit 1
  fi
  
  # Stop any existing containers
  success "Stopping existing containers..." "time"
  docker compose down
  
  # Start the services
  success "Starting services..." "time"
  docker compose up -d
  
  # Check if services are healthy
  success "Waiting for services to be healthy..." "time"
  sleep 10
  
  if docker compose ps | grep -q "Exit"; then
    error "Some services failed to start. Please check docker-compose logs" "time"
    docker compose logs
    exit 1
  fi
  
  success "Local environment started successfully" "time"
  success "Services running:" "time"
  docker compose ps
elif [ "$action" = "stop_local" ]; then
  success "Stopping local environment..." "time"
  docker compose down
  success "Local environment stopped successfully" "time"
elif [ "$action" = "tear_down" ]; then
  success "Cleaning up the entire database..." "time"
  curl -X POST http://localhost:8088/api/v1/ops/databases/cleanup
  success "Database cleanup completed successfully" "time"
elif [ "$action" = "mock_up" ]; then
  success "Populating sample data..." "time"
  curl -X POST http://localhost:8088/api/v1/ops/accounts/samples
  success "Sample data populated successfully" "time"
elif [ "$action" = "performance_test" ]; then
  success "Running performance test..." "time"
  k6 run performance_test.js
  success "Performance test completed" "time"
fi