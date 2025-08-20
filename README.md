# Spring Kafka Integration Demo

A comprehensive Kafka-based event-driven system with Spring Boot microservices.

## System Architecture

This project demonstrates a production-ready Kafka integration with:

- **Producer Service**: Handles event publishing to Kafka topics
- **Consumer Service**: Processes events from Kafka topics  
- **Web Console**: Interactive dashboard for testing and monitoring
- **Notification API**: Simple notification service

## Quick Start

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Kubernetes cluster (for production deployment)

### Local Development
```bash
# Start Kafka and dependencies
docker-compose up -d

# Run producer service
cd producer && mvn spring-boot:run

# Run consumer service  
cd consumer && mvn spring-boot:run
```

### Production Deployment
```bash
# Deploy to Kubernetes
kubectl apply -f k8s/

# Access web console
open http://kafka.ciscloudlab.link/web
```

## Features

- ✅ Event-driven architecture with Kafka
- ✅ Spring Boot microservices
- ✅ Interactive web console
- ✅ Health monitoring and metrics
- ✅ Kubernetes deployment ready
- ✅ CI/CD pipeline with GitHub Actions

## API Endpoints

### Producer API
- `POST /api/v1/messages/user` - Send user events
- `POST /api/v1/messages/business` - Send business events
- `POST /api/v1/messages/system` - Send system events
- `GET /api/v1/health` - Health check

### Consumer API  
- `GET /api/v1/status` - Consumer status
- `GET /api/v1/health` - Health check

### Web Console
- `GET /web` - Interactive dashboard

## Monitoring

- Health endpoints available at `/actuator/health`
- Metrics available at `/actuator/metrics`
- Prometheus metrics enabled

## Build Status

Last updated: 2025-08-20 - Trigger build after rollback
# CI/CD Test - Wed 20 Aug 2025 21:05:54 CST
