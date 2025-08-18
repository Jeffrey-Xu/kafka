# 🚀 Spring Boot + Kafka Integration Demo

A complete demonstration of Spring Boot applications integrated with Apache Kafka, featuring producer and consumer services with MySQL persistence, deployed on Kubernetes with full CI/CD pipeline.

## 🏗️ Architecture Overview

```
External Users → REST API → Producer App → Kafka → Consumer App → MySQL Database
                                ↓                      ↓
                         Statistics & Logs    Processed Events & Analytics
```

### Components

- **Producer Application** (Port 8080): REST API for sending messages to Kafka
- **Consumer Application** (Port 8081): Kafka listeners for processing messages
- **MySQL Database**: Shared persistence layer for statistics and processed data
- **Apache Kafka**: Message broker for event streaming
- **Kubernetes**: Container orchestration platform

## 📦 Project Structure

```
spring-kafka-integration/
├── common/                 # Shared models and utilities
├── producer/              # Kafka producer with REST API
├── consumer/              # Kafka consumer with event processing
├── k8s/                   # Kubernetes deployment manifests
├── .github/workflows/     # CI/CD pipeline definitions
└── docker-compose.yml     # Local development setup
```

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Maven 3.9+
- Docker & Docker Compose
- Kubernetes cluster (for production deployment)

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/Jeffrey-Xu/spring-kafka-integration.git
   cd spring-kafka-integration
   ```

2. **Start local infrastructure**
   ```bash
   docker-compose up -d
   ```

3. **Build and run applications**
   ```bash
   # Build all modules
   mvn clean package
   
   # Run producer (terminal 1)
   cd producer && mvn spring-boot:run
   
   # Run consumer (terminal 2)
   cd consumer && mvn spring-boot:run
   ```

### Testing the System

1. **Send a user event**
   ```bash
   curl -X POST http://localhost:8080/api/v1/messages/user \
     -H "Content-Type: application/json" \
     -d '{
       "userId": "user123",
       "action": "LOGIN",
       "sessionId": "session456",
       "ipAddress": "192.168.1.100",
       "source": "web-app"
     }'
   ```

2. **Send a business event**
   ```bash
   curl -X POST http://localhost:8080/api/v1/messages/business \
     -H "Content-Type: application/json" \
     -d '{
       "orderId": "order789",
       "customerId": "customer123",
       "eventType": "ORDER_CREATED",
       "amount": 99.99,
       "currency": "USD",
       "source": "e-commerce"
     }'
   ```

3. **Check processing status**
   ```bash
   curl http://localhost:8081/api/v1/status
   ```

4. **View producer statistics**
   ```bash
   curl http://localhost:8080/api/v1/messages/stats
   ```

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker addresses | `localhost:9092` |
| `MYSQL_HOST` | MySQL host | `localhost` |
| `MYSQL_PORT` | MySQL port | `3306` |
| `MYSQL_DATABASE` | Database name | `kafka_demo` |
| `MYSQL_USERNAME` | Database username | `kafka_user` |
| `MYSQL_PASSWORD` | Database password | `kafka_pass` |

### Kafka Topics

- `user-events`: User activity events (3 partitions)
- `business-events`: Business transaction events (5 partitions)
- `system-events`: System operational events (2 partitions)

## 🐳 Docker Deployment

### Build Images

```bash
# Build producer image
docker build -t spring-kafka-producer:latest ./producer

# Build consumer image
docker build -t spring-kafka-consumer:latest ./consumer
```

### Run with Docker Compose

```bash
docker-compose up -d
```

This starts:
- Kafka + ZooKeeper
- MySQL database
- Producer application
- Consumer application
- Kafka UI (http://localhost:8080)

## ☸️ Kubernetes Deployment

### Prerequisites

- Kubernetes cluster (1.28+)
- kubectl configured
- MySQL database deployed (see k8s/mysql/)

### Deploy Applications

```bash
# Deploy producer
kubectl apply -f k8s/producer/

# Deploy consumer
kubectl apply -f k8s/consumer/

# Deploy ingress
kubectl apply -f k8s/ingress/
```

### Access Applications

- Producer API: `https://kafka-demo.your-domain.com/producer`
- Consumer API: `https://kafka-demo.your-domain.com/consumer`
- Health checks: `/actuator/health`
- Metrics: `/actuator/prometheus`

## 🧪 Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
mvn verify -P integration-tests
```

### Load Testing

```bash
# Generate 1000 user events
for i in {1..1000}; do
  curl -X POST http://localhost:8080/api/v1/messages/user \
    -H "Content-Type: application/json" \
    -d "{\"userId\":\"user$i\",\"action\":\"BROWSE\",\"source\":\"load-test\"}" &
done
wait
```

## 📊 Monitoring

### Health Checks

- Producer: `http://localhost:8080/actuator/health`
- Consumer: `http://localhost:8081/actuator/health`

### Metrics

- Prometheus metrics: `/actuator/prometheus`
- Application metrics: `/actuator/metrics`

### Database Queries

```sql
-- Check message processing stats
SELECT event_type, COUNT(*) as count, AVG(processing_time_ms) as avg_time
FROM processed_messages 
GROUP BY event_type;

-- View recent user activity
SELECT * FROM user_events 
ORDER BY created_at DESC 
LIMIT 10;
```

## 🔄 CI/CD Pipeline

The project includes GitHub Actions workflows for:

- **Continuous Integration**: Build, test, and code quality checks
- **Container Building**: Docker image creation and push to registry
- **Deployment**: Automated deployment to Kubernetes

### Pipeline Triggers

- Push to `main`: Full CI/CD pipeline
- Pull requests: CI validation only
- Manual: Production deployment

## 🛠️ Development

### Adding New Event Types

1. Create event model in `common/src/main/java/com/jeffreyxu/kafka/common/model/`
2. Add to `BaseEvent` JSON subtypes
3. Create producer endpoint in `MessageController`
4. Add consumer listener in appropriate listener class
5. Update database schema if needed

### Configuration Profiles

- `dev`: Local development with embedded H2
- `test`: Testing with TestContainers
- `kubernetes`: Production deployment on K8s

## 🐛 Troubleshooting

### Common Issues

1. **Kafka Connection Failed**
   - Check `KAFKA_BOOTSTRAP_SERVERS` configuration
   - Verify Kafka is running and accessible

2. **Database Connection Failed**
   - Verify MySQL is running
   - Check database credentials and host

3. **Messages Not Being Consumed**
   - Check consumer group configuration
   - Verify topic names match
   - Check consumer logs for errors

### Debugging

```bash
# Check application logs
kubectl logs -f deployment/spring-kafka-producer
kubectl logs -f deployment/spring-kafka-consumer

# Check Kafka topics
kubectl exec -it kafka-pod -- kafka-topics.sh --list --bootstrap-server localhost:9092

# Check database
kubectl exec -it mysql-pod -- mysql -u kafka_user -p kafka_demo
```

## 📚 Documentation

- [API Documentation](docs/api.md)
- [Deployment Guide](docs/deployment.md)
- [Architecture Decision Records](docs/adr/)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Apache Kafka for the robust messaging platform
- Kubernetes community for container orchestration

---

**Built with ❤️ for learning and demonstration purposes**
