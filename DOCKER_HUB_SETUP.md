# GitHub CI/CD + Docker Hub Setup Guide

This guide explains how to set up automated CI/CD deployment using GitHub Actions and Docker Hub.

## 🏗️ Architecture Overview

```
GitHub Repository → GitHub Actions → Docker Hub → Kubernetes Cluster
     ↓                    ↓              ↓            ↓
  Code Push         Build & Test    Store Images   Deploy Apps
```

## 📋 Prerequisites

1. **GitHub Repository**: https://github.com/Jeffrey-Xu/kafka
2. **Docker Hub Repository**: https://hub.docker.com/repository/docker/jeffreyxu2025/kafka
3. **Kubernetes Cluster**: Running at 52.90.236.10
4. **Docker Hub Token**: For authentication

## 🚀 Setup Steps

### Step 1: Set up GitHub Repository

```bash
cd /Users/jeffreyxu/Documents/lab/kafka/spring-kafka-integration

# Add GitHub remote
git remote add origin https://github.com/Jeffrey-Xu/kafka.git

# Push code to GitHub
git branch -M main
git push -u origin main
```

### Step 2: Create Docker Hub Access Token

1. Go to [Docker Hub](https://hub.docker.com)
2. Click your profile → Account Settings → Security
3. Click "New Access Token"
4. Name: `github-actions-kafka`
5. Permissions: `Read, Write, Delete`
6. Copy the generated token

### Step 3: Configure GitHub Secrets

Go to your repository → Settings → Secrets and variables → Actions

Add these secrets:

#### Required Secrets:
- **`DOCKER_HUB_TOKEN`**: Your Docker Hub access token (from Step 2)
- **`SSH_PRIVATE_KEY`**: Your EC2 private key content
- **`K8S_HOST`**: `52.90.236.10`
- **`K8S_USER`**: `ubuntu`

#### To get the SSH private key:
```bash
# Copy your EC2 private key content
cat ~/.ssh/my-ec2-key.pem
```
Copy the entire content (including `-----BEGIN RSA PRIVATE KEY-----` and `-----END RSA PRIVATE KEY-----`) and add it as the `SSH_PRIVATE_KEY` secret.

### Step 4: Verify Docker Hub Repository Structure

The CI/CD pipeline will create these images in your Docker Hub repository:

```
jeffreyxu2025/kafka:producer-latest
jeffreyxu2025/kafka:producer-{commit-sha}
jeffreyxu2025/kafka:consumer-latest  
jeffreyxu2025/kafka:consumer-{commit-sha}
```

## 🔄 CI/CD Pipeline Workflow

### Trigger Events:
- **Push to `main`**: Full pipeline (test → build → deploy)
- **Push to `develop`**: Test and build only
- **Pull Request**: Test only

### Pipeline Stages:

1. **Test Stage**:
   - Run unit tests with Maven
   - Generate test reports
   - Code coverage analysis

2. **Build & Push Stage** (main branch only):
   - Build Docker images for AMD64 platform
   - Push to Docker Hub with commit SHA and latest tags
   - Use multi-stage builds for optimization

3. **Deploy Stage** (main branch only):
   - Update Kubernetes manifests with new image tags
   - SSH to Kubernetes master node
   - Deploy applications using kubectl
   - Verify deployment health

## 📊 Image Management

### Docker Hub Images:
```bash
# Producer images
jeffreyxu2025/kafka:producer-latest      # Always points to latest main build
jeffreyxu2025/kafka:producer-abc123def   # Specific commit version

# Consumer images  
jeffreyxu2025/kafka:consumer-latest      # Always points to latest main build
jeffreyxu2025/kafka:consumer-abc123def   # Specific commit version
```

### Benefits of Docker Hub:
- ✅ Free public repositories
- ✅ No AWS costs
- ✅ Better network connectivity
- ✅ Automatic vulnerability scanning
- ✅ Easy image management UI

## 🛠️ Testing the Setup

### Step 1: Push to GitHub
```bash
# Make a small change to trigger the pipeline
echo "# Updated $(date)" >> README.md
git add README.md
git commit -m "Test CI/CD pipeline with Docker Hub"
git push origin main
```

### Step 2: Monitor Pipeline
1. Go to: https://github.com/Jeffrey-Xu/kafka/actions
2. Watch the pipeline execution
3. Check each stage: Test → Build → Deploy

### Step 3: Verify Docker Hub
1. Go to: https://hub.docker.com/repository/docker/jeffreyxu2025/kafka
2. Check for new images with latest commit SHA
3. Verify image sizes and build times

### Step 4: Verify Kubernetes Deployment
```bash
# SSH to your Kubernetes cluster
ssh -i ~/.ssh/my-ec2-key.pem ubuntu@52.90.236.10

# Check deployment status
kubectl get pods -n kafka-demo
kubectl get services -n kafka-demo
kubectl get ingress -n kafka-demo

# Check application health
kubectl exec deployment/spring-kafka-producer -n kafka-demo -- curl -f http://localhost:8080/actuator/health
kubectl exec deployment/spring-kafka-consumer -n kafka-demo -- curl -f http://localhost:8081/api/consumer/health
```

## 🌐 Access Your Applications

After successful deployment:

- **Producer API**: http://kafka.ciscloudlab.link/producer
- **Consumer API**: http://kafka.ciscloudlab.link/consumer  
- **Producer Health**: http://kafka.ciscloudlab.link/producer/actuator/health
- **Consumer Health**: http://kafka.ciscloudlab.link/consumer/api/consumer/health
- **Consumer Stats**: http://kafka.ciscloudlab.link/consumer/api/consumer/stats

## 🔧 Troubleshooting

### Common Issues:

1. **Docker Hub Authentication Failed**:
   - Verify `DOCKER_HUB_TOKEN` secret is correct
   - Check token permissions (Read, Write, Delete)

2. **SSH Connection Failed**:
   - Verify `SSH_PRIVATE_KEY` secret contains full key content
   - Check `K8S_HOST` and `K8S_USER` values

3. **Image Pull Errors**:
   - Ensure Docker Hub repository is public
   - Verify image tags are correct in manifests

4. **Deployment Timeout**:
   - Check Kubernetes cluster resources
   - Verify MySQL database is running
   - Check application logs for startup issues

### Debug Commands:
```bash
# Check GitHub Actions logs in the web UI

# Check Docker Hub images
docker pull jeffreyxu2025/kafka:producer-latest
docker pull jeffreyxu2025/kafka:consumer-latest

# Check Kubernetes deployment
kubectl describe deployment spring-kafka-producer -n kafka-demo
kubectl logs -f deployment/spring-kafka-producer -n kafka-demo
kubectl get events -n kafka-demo --sort-by='.lastTimestamp'
```

## 🎯 Next Steps

1. **Push code to GitHub** to trigger first pipeline
2. **Monitor pipeline execution** in GitHub Actions
3. **Verify images** appear in Docker Hub
4. **Test deployed applications** via ingress URLs
5. **Set up monitoring** and alerting for production

This setup provides:
- ✅ Automated testing on every push
- ✅ Secure image storage in Docker Hub
- ✅ Automated deployment to Kubernetes
- ✅ Health verification and rollback capabilities
- ✅ Complete audit trail of deployments

Your Spring Boot + Kafka integration system will be automatically deployed whenever you push to the main branch!
