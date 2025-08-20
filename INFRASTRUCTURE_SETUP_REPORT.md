# Infrastructure Setup Report - Kafka Event-Driven Microservices

## 📋 Executive Summary

This document provides a comprehensive guide for setting up a production-ready Kafka-based event-driven microservices architecture on AWS with Kubernetes, including CI/CD pipelines, container orchestration, and external access configuration.

**Project Overview:**
- **Architecture**: Event-driven microservices with Kafka
- **Infrastructure**: AWS EC2 + Kubernetes cluster
- **Applications**: Spring Boot Producer/Consumer + Nginx Web Console
- **CI/CD**: GitHub Actions with Docker Hub integration
- **External Access**: AWS NLB + Nginx Ingress Controller
- **Monitoring**: Built-in health checks and metrics

---

## 🏗️ Infrastructure Architecture

### **High-Level Architecture**
```
Internet → Route53 DNS → AWS NLB → Nginx Ingress Controller → Services → Pods
                                                           ↓
                                              Kafka Cluster (KRaft Mode)
                                                           ↓
                                              MySQL Database (Persistent Storage)
```

### **Component Overview**
```
┌─────────────────────────────────────────────────────────────────┐
│                        AWS Infrastructure                        │
├─────────────────────────────────────────────────────────────────┤
│  Route53 DNS Records                                            │
│  ├── master01.ciscloudlab.link → 52.71.164.37                  │
│  ├── worker01.ciscloudlab.link → 18.234.54.66                  │
│  ├── worker02.ciscloudlab.link → 3.84.85.255                   │
│  └── kafka.ciscloudlab.link → NLB Endpoint                     │
├─────────────────────────────────────────────────────────────────┤
│  EC2 Instances (3-node Kubernetes cluster)                     │
│  ├── master01 (i-0b60f884f8f2c0401) - Control Plane           │
│  ├── worker01 (i-057f34c111cf1a553) - Worker Node              │
│  └── worker02 (i-080b3d285dc083438) - Worker Node              │
├─────────────────────────────────────────────────────────────────┤
│  Kubernetes Components                                          │
│  ├── Nginx Ingress Controller (3 replicas)                     │
│  ├── Local Path Provisioner (Storage)                          │
│  ├── Flannel CNI (Networking)                                  │
│  └── CoreDNS (Service Discovery)                               │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🚀 AWS Infrastructure Setup

### **1. EC2 Instance Configuration**

**Instance Specifications:**
```yaml
Instance Type: t3.medium (2 vCPU, 4GB RAM)
Operating System: Ubuntu 22.04 LTS
Storage: 20GB GP3 SSD
Network: VPC with public subnets
Security Groups: 
  - SSH (22): Your IP
  - Kubernetes API (6443): Cluster nodes
  - NodePort Range (30000-32767): Load balancer
  - Flannel VXLAN (8472): Cluster nodes
```

**Security Group Rules:**
```bash
# SSH Access
Port 22: Source = Your IP/32

# Kubernetes API Server
Port 6443: Source = Cluster CIDR

# NodePort Services
Ports 30000-32767: Source = Load Balancer

# Flannel VXLAN
Port 8472/UDP: Source = Cluster CIDR

# Kubelet API
Port 10250: Source = Cluster CIDR
```

### **2. Route53 DNS Configuration**

**DNS Records Setup:**
```bash
# A Records for cluster nodes
master01.ciscloudlab.link → 52.71.164.37
worker01.ciscloudlab.link → 18.234.54.66  
worker02.ciscloudlab.link → 3.84.85.255

# CNAME for application access
kafka.ciscloudlab.link → NLB DNS Name
```

**DNS Management Script:**
```bash
#!/bin/bash
# Update Route53 records
aws route53 change-resource-record-sets \
  --hosted-zone-id Z123456789 \
  --change-batch file://dns-changes.json
```

---

## ⚙️ Kubernetes Cluster Setup

### **1. Cluster Initialization**

**Master Node Setup:**
```bash
# Initialize cluster
sudo kubeadm init \
  --pod-network-cidr=10.244.0.0/16 \
  --service-cidr=10.96.0.0/12 \
  --apiserver-advertise-address=<MASTER_IP>

# Configure kubectl
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

**Worker Node Join:**
```bash
# Join worker nodes
sudo kubeadm join <MASTER_IP>:6443 \
  --token <TOKEN> \
  --discovery-token-ca-cert-hash sha256:<HASH>
```

### **2. Network Configuration**

**Flannel CNI Installation:**
```bash
# Install Flannel for pod networking
kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml
```

**Network Specifications:**
```yaml
Pod CIDR: 10.244.0.0/16
Service CIDR: 10.96.0.0/12
CNI: Flannel VXLAN
DNS: CoreDNS
```

### **3. Storage Configuration**

**Local Path Provisioner:**
```bash
# Install local-path storage
kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/v0.0.24/deploy/local-path-storage.yaml

# Set as default storage class
kubectl patch storageclass local-path -p '{"metadata": {"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}'
```

---

## 🔄 CI/CD Pipeline Configuration

### **1. GitHub Actions Setup**

**Repository Structure:**
```
spring-kafka-integration/
├── .github/workflows/
│   ├── ci-cd-pipeline.yml      # Main application pipeline
│   └── nginx-web.yml           # Web console pipeline
├── k8s/                        # Kubernetes manifests
├── nginx-web/                  # Web console source
├── producer/                   # Producer microservice
├── consumer/                   # Consumer microservice
└── docker-compose.yml          # Local development
```

**Main CI/CD Pipeline Features:**
```yaml
Triggers:
  - Push to main branch
  - Pull requests
  - Manual workflow dispatch

Stages:
  1. Test & Build
  2. Docker Build & Push
  3. Deploy to Kubernetes
  4. Verify Deployment
  5. Notify Results
```

### **2. GitHub Secrets Configuration**

**Required Secrets:**
```bash
# Docker Hub Authentication
DOCKER_HUB_TOKEN=dckr_pat_xxxxx

# Kubernetes Access
K8S_TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6Ixxxxx

# Optional: Slack/Discord notifications
SLACK_WEBHOOK_URL=https://hooks.slack.com/xxxxx
```

**Service Account Setup:**
```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: github-actions-deployer
  namespace: kafka-demo
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: github-actions-deployer
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
- kind: ServiceAccount
  name: github-actions-deployer
  namespace: kafka-demo
```

### **3. Docker Hub Configuration**

**Repository Setup:**
```bash
# Docker Hub repositories
jeffreyxu2025/kafka:producer-latest
jeffreyxu2025/kafka:consumer-latest
jeffreyxu2025/kafka:nginx-web-console-latest

# Automated builds from GitHub
- Source: GitHub repository
- Build rules: Tag-based and branch-based
- Webhooks: Trigger on push
```

**Multi-stage Dockerfile Example:**
```dockerfile
# Producer Dockerfile
FROM openjdk:17-jdk-slim as builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jre-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 🌐 External Access Configuration

### **1. Nginx Ingress Controller**

**Installation:**
```bash
# Install Nginx Ingress Controller
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.2/deploy/static/provider/cloud/deploy.yaml

# Verify installation
kubectl get pods -n ingress-nginx
kubectl get services -n ingress-nginx
```

**Configuration:**
```yaml
# Ingress resource
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: kafka-demo-ingress
  namespace: kafka-demo
spec:
  ingressClassName: nginx
  rules:
  - host: kafka.ciscloudlab.link
    http:
      paths:
      - path: /api/v1/messages
        pathType: Prefix
        backend:
          service:
            name: producer-service
            port:
              number: 8080
      - path: /api/consumer
        pathType: Prefix
        backend:
          service:
            name: consumer-service
            port:
              number: 8080
      - path: /web
        pathType: Prefix
        backend:
          service:
            name: nginx-web-console-service
            port:
              number: 80
```

### **2. AWS Network Load Balancer**

**NLB Configuration:**
```yaml
Type: Network Load Balancer
Scheme: Internet-facing
IP Address Type: IPv4
Listeners:
  - Port 80 (HTTP)
  - Port 443 (HTTPS) - Optional

Target Groups:
  - Protocol: TCP
  - Port: 80
  - Health Check: HTTP /healthz
  - Targets: Ingress Controller NodePorts
```

**Target Group Health Checks:**
```yaml
Protocol: HTTP
Path: /healthz
Port: Traffic port
Healthy threshold: 2
Unhealthy threshold: 2
Timeout: 6 seconds
Interval: 10 seconds
```

---

## 🐳 Application Architecture

### **1. Microservices Overview**

**Producer Service:**
```yaml
Technology: Spring Boot 3.x + Java 17
Purpose: REST API for event ingestion
Endpoints:
  - POST /api/v1/messages/user
  - POST /api/v1/messages/business
  - POST /api/v1/messages/system
  - GET /api/v1/messages/stats
  - GET /actuator/health

Resources:
  CPU: 200m-500m
  Memory: 512Mi-1Gi
  Replicas: 2
```

**Consumer Service:**
```yaml
Technology: Spring Boot 3.x + Java 17
Purpose: Event processing and database persistence
Endpoints:
  - GET /api/consumer/health
  - GET /api/consumer/stats
  
Resources:
  CPU: 200m-500m
  Memory: 512Mi-1Gi
  Replicas: 2
```

**Web Console:**
```yaml
Technology: Nginx + HTML/CSS/JavaScript
Purpose: User interface for event management
Features:
  - Multi-event type support
  - Real-time monitoring
  - Enhanced UI/UX
  
Resources:
  CPU: 50m-100m
  Memory: 64Mi-128Mi
  Replicas: 1
```

### **2. Kafka Configuration**

**Kafka Cluster (KRaft Mode):**
```yaml
Version: 3.8.0
Mode: KRaft (no Zookeeper)
Brokers: 3 replicas
Topics:
  - user-events (partitions: 3, replication: 2)
  - business-events (partitions: 3, replication: 2)
  - system-events (partitions: 3, replication: 2)

Resources:
  CPU: 500m-1000m per broker
  Memory: 1Gi-2Gi per broker
  Storage: 10Gi per broker
```

**Topic Configuration:**
```bash
# Create topics with proper configuration
kafka-topics.sh --create \
  --topic user-events \
  --partitions 3 \
  --replication-factor 2 \
  --config retention.ms=604800000
```

### **3. Database Configuration**

**MySQL Setup:**
```yaml
Version: 8.0
Storage: 5Gi PVC (local-path)
Configuration:
  - InnoDB engine
  - UTF8MB4 charset
  - Proper indexing for event queries
  
Resources:
  CPU: 100m-300m
  Memory: 256Mi-512Mi
  Storage: 5Gi
```

**Database Schema:**
```sql
CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    event_data JSON NOT NULL,
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    correlation_id VARCHAR(100),
    INDEX idx_event_type (event_type),
    INDEX idx_processed_at (processed_at),
    INDEX idx_correlation_id (correlation_id)
);
```

---

## 📊 Monitoring and Observability

### **1. Health Checks**

**Application Health Endpoints:**
```bash
# Producer health
curl http://kafka.ciscloudlab.link/actuator/health

# Consumer health  
curl http://kafka.ciscloudlab.link/api/consumer/health

# Nginx health
curl http://kafka.ciscloudlab.link/web/nginx-health
```

**Kubernetes Health Checks:**
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 5
  periodSeconds: 5
```

### **2. Metrics and Statistics**

**Producer Metrics:**
```json
{
  "totalMessages": 150,
  "successRate": 98.5,
  "averageLatency": 45.2,
  "topicBreakdown": {
    "user-events": 80,
    "business-events": 50,
    "system-events": 20
  }
}
```

**Consumer Metrics:**
```json
{
  "totalProcessedMessages": 145,
  "successRate": 100.0,
  "averageProcessingTime": 12.5,
  "databaseConnections": 5,
  "uptime": 3600
}
```

---

## 🔧 Deployment Procedures

### **1. Initial Deployment**

**Step-by-Step Process:**
```bash
# 1. Prepare infrastructure
terraform apply -var-file="production.tfvars"

# 2. Configure Kubernetes cluster
./setup-k8s-cluster.sh

# 3. Deploy applications
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/kafka/
kubectl apply -f k8s/mysql/
kubectl apply -f k8s/producer/
kubectl apply -f k8s/consumer/
kubectl apply -f k8s/ingress/

# 4. Verify deployment
kubectl get all -n kafka-demo
curl http://kafka.ciscloudlab.link/actuator/health
```

### **2. CI/CD Deployment**

**Automated Pipeline:**
```yaml
1. Code Push → GitHub
2. GitHub Actions Triggered
3. Tests Execute
4. Docker Images Built
5. Images Pushed to Docker Hub
6. Kubernetes Deployment Updated
7. Health Checks Performed
8. Notifications Sent
```

**Rollback Procedure:**
```bash
# Rollback to previous version
kubectl rollout undo deployment/producer -n kafka-demo
kubectl rollout undo deployment/consumer -n kafka-demo

# Check rollout status
kubectl rollout status deployment/producer -n kafka-demo
```

---

## 🛡️ Security Configuration

### **1. Network Security**

**Security Groups:**
```yaml
Ingress Rules:
  - SSH (22): Admin IP only
  - HTTPS (443): 0.0.0.0/0
  - HTTP (80): 0.0.0.0/0
  - Kubernetes API (6443): Cluster nodes only
  - NodePorts (30000-32767): Load balancer only

Egress Rules:
  - All traffic: 0.0.0.0/0 (for package updates)
```

**Network Policies:**
```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: kafka-demo-network-policy
spec:
  podSelector:
    matchLabels:
      app: kafka-demo
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: kafka-demo
```

### **2. RBAC Configuration**

**Service Account Permissions:**
```yaml
# Minimal permissions for CI/CD
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: deployer-role
  namespace: kafka-demo
rules:
- apiGroups: ["apps", ""]
  resources: ["deployments", "services", "pods"]
  verbs: ["get", "list", "create", "update", "patch"]
```

### **3. Secrets Management**

**Kubernetes Secrets:**
```bash
# Database credentials
kubectl create secret generic mysql-secret \
  --from-literal=username=kafka_user \
  --from-literal=password=secure_password \
  -n kafka-demo

# Docker registry credentials
kubectl create secret docker-registry docker-hub-secret \
  --docker-server=docker.io \
  --docker-username=jeffreyxu2025 \
  --docker-password=$DOCKER_HUB_TOKEN \
  -n kafka-demo
```

---

## 📈 Performance Optimization

### **1. Resource Allocation**

**Recommended Resource Limits:**
```yaml
Producer Service:
  requests:
    cpu: 200m
    memory: 512Mi
  limits:
    cpu: 500m
    memory: 1Gi

Consumer Service:
  requests:
    cpu: 200m
    memory: 512Mi
  limits:
    cpu: 500m
    memory: 1Gi

Kafka Brokers:
  requests:
    cpu: 500m
    memory: 1Gi
  limits:
    cpu: 1000m
    memory: 2Gi
```

### **2. Scaling Configuration**

**Horizontal Pod Autoscaler:**
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: producer-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: producer
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

### **3. Performance Tuning**

**JVM Optimization:**
```bash
# Producer JVM settings
JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Consumer JVM settings  
JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:+UseStringDeduplication"
```

**Kafka Tuning:**
```properties
# Broker configuration
num.network.threads=8
num.io.threads=16
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600
log.retention.hours=168
log.segment.bytes=1073741824
```

---

## 🔍 Troubleshooting Guide

### **1. Common Issues**

**Pod Startup Issues:**
```bash
# Check pod status
kubectl get pods -n kafka-demo

# Check pod logs
kubectl logs <pod-name> -n kafka-demo

# Describe pod for events
kubectl describe pod <pod-name> -n kafka-demo
```

**Network Connectivity Issues:**
```bash
# Test internal DNS
kubectl run test-pod --rm -i --tty --image=busybox -- nslookup producer-service.kafka-demo.svc.cluster.local

# Test external access
curl -I http://kafka.ciscloudlab.link/actuator/health

# Check ingress configuration
kubectl describe ingress kafka-demo-ingress -n kafka-demo
```

**Database Connection Issues:**
```bash
# Check MySQL pod
kubectl get pods -l app=mysql -n kafka-demo

# Test database connectivity
kubectl exec -it <mysql-pod> -n kafka-demo -- mysql -u root -p

# Check persistent volume
kubectl get pv,pvc -n kafka-demo
```

### **2. Performance Issues**

**High CPU Usage:**
```bash
# Check resource usage
kubectl top pods -n kafka-demo

# Check HPA status
kubectl get hpa -n kafka-demo

# Scale manually if needed
kubectl scale deployment producer --replicas=5 -n kafka-demo
```

**Memory Issues:**
```bash
# Check memory usage
kubectl describe node <node-name>

# Check for OOMKilled pods
kubectl get events --sort-by='.lastTimestamp' -n kafka-demo
```

### **3. CI/CD Issues**

**GitHub Actions Failures:**
```bash
# Common issues:
1. Docker Hub authentication failure
2. Kubernetes token expiration
3. Resource conflicts during deployment
4. Image pull failures

# Solutions:
1. Regenerate DOCKER_HUB_TOKEN
2. Update K8S_TOKEN secret
3. Check resource quotas
4. Verify image tags and availability
```

---

## 📚 Best Practices

### **1. Development Workflow**

**Git Workflow:**
```bash
# Feature development
git checkout -b feature/new-feature
git commit -m "feat: add new feature"
git push origin feature/new-feature

# Create pull request
# CI/CD runs automatically
# Merge after approval
```

**Testing Strategy:**
```yaml
Unit Tests: 80%+ coverage
Integration Tests: API endpoints
End-to-End Tests: Critical user flows
Performance Tests: Load testing with JMeter
```

### **2. Operational Practices**

**Monitoring:**
```bash
# Regular health checks
curl http://kafka.ciscloudlab.link/actuator/health

# Resource monitoring
kubectl top nodes
kubectl top pods -n kafka-demo

# Log monitoring
kubectl logs -f deployment/producer -n kafka-demo
```

**Backup Strategy:**
```bash
# Database backups
kubectl exec mysql-pod -n kafka-demo -- mysqldump kafka_db > backup.sql

# Configuration backups
kubectl get all -n kafka-demo -o yaml > kafka-demo-backup.yaml
```

### **3. Security Practices**

**Regular Updates:**
```bash
# Update base images monthly
# Scan images for vulnerabilities
# Rotate secrets quarterly
# Review RBAC permissions
```

**Access Control:**
```bash
# Use least privilege principle
# Implement network policies
# Enable audit logging
# Regular security reviews
```

---

## 🎯 Future Enhancements

### **1. Planned Improvements**

**Infrastructure:**
- Multi-AZ deployment for high availability
- Auto-scaling groups for worker nodes
- Managed Kubernetes (EKS) migration
- Service mesh implementation (Istio)

**Applications:**
- Event sourcing implementation
- CQRS pattern adoption
- Real-time analytics dashboard
- Advanced monitoring with Prometheus/Grafana

**Security:**
- SSL/TLS termination at ingress
- OAuth2/OIDC authentication
- Secrets management with AWS Secrets Manager
- Network encryption with service mesh

### **2. Scalability Roadmap**

**Phase 1: Current State**
- 3-node Kubernetes cluster
- Basic monitoring and alerting
- Manual scaling procedures

**Phase 2: Enhanced Automation**
- Cluster autoscaling
- Advanced monitoring stack
- Automated backup procedures
- Disaster recovery planning

**Phase 3: Production Scale**
- Multi-region deployment
- Advanced security controls
- Compliance frameworks
- Enterprise monitoring

---

## 📖 Reference Documentation

### **1. External Resources**

**Kubernetes:**
- [Official Kubernetes Documentation](https://kubernetes.io/docs/)
- [Nginx Ingress Controller](https://kubernetes.github.io/ingress-nginx/)
- [Local Path Provisioner](https://github.com/rancher/local-path-provisioner)

**Kafka:**
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Kafka Reference](https://spring.io/projects/spring-kafka)
- [Kafka KRaft Mode](https://kafka.apache.org/documentation/#kraft)

**AWS:**
- [AWS Load Balancer Controller](https://kubernetes-sigs.github.io/aws-load-balancer-controller/)
- [Route53 Documentation](https://docs.aws.amazon.com/route53/)
- [EC2 User Guide](https://docs.aws.amazon.com/ec2/)

### **2. Internal Documentation**

**Repository Structure:**
```
├── INFRASTRUCTURE_SETUP_REPORT.md  # This document
├── nginx-web/DEVELOPMENT.md         # Web console development
├── k8s/                            # Kubernetes manifests
├── docs/                           # Additional documentation
└── scripts/                        # Automation scripts
```

**Key Configuration Files:**
- `.github/workflows/ci-cd-pipeline.yml` - Main CI/CD pipeline
- `.github/workflows/nginx-web.yml` - Web console pipeline
- `k8s/ingress/ingress.yaml` - Ingress configuration
- `docker-compose.yml` - Local development setup

---

## ✅ Conclusion

This infrastructure setup provides a robust, scalable, and maintainable foundation for event-driven microservices architecture. The combination of Kubernetes orchestration, automated CI/CD pipelines, and proper external access configuration creates a production-ready environment suitable for enterprise applications.

**Key Success Factors:**
1. **Automated Deployment**: GitHub Actions CI/CD pipeline
2. **Scalable Architecture**: Kubernetes with proper resource management
3. **External Access**: AWS NLB + Nginx Ingress for reliable connectivity
4. **Monitoring**: Built-in health checks and metrics
5. **Security**: RBAC, network policies, and secrets management
6. **Documentation**: Comprehensive setup and troubleshooting guides

**Template Usage:**
This document serves as a complete template for setting up similar event-driven microservices architectures. Follow the step-by-step procedures, adapt the configurations to your specific requirements, and use the troubleshooting guide for operational support.

---

**Document Version:** 1.0  
**Last Updated:** 2025-08-20  
**Author:** Infrastructure Team  
**Review Date:** 2025-11-20
