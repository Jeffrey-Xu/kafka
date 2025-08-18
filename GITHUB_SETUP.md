# GitHub CI/CD Setup Guide

This guide explains how to set up automated CI/CD deployment using GitHub Actions and AWS ECR.

## 🏗️ Architecture Overview

```
GitHub Repository → GitHub Actions → AWS ECR → Kubernetes Cluster
     ↓                    ↓              ↓            ↓
  Code Push         Build & Test    Store Images   Deploy Apps
```

## 📋 Prerequisites

1. **GitHub Repository**: Create a new repository on GitHub
2. **AWS ECR**: Repositories already created:
   - `spring-kafka-producer` 
   - `spring-kafka-consumer`
3. **Kubernetes Cluster**: Running and accessible
4. **AWS Credentials**: For ECR access

## 🚀 Setup Steps

### Step 1: Create GitHub Repository

1. Go to [GitHub](https://github.com) and create a new repository
2. Name it: `spring-kafka-integration`
3. Make it public or private (your choice)
4. Don't initialize with README (we already have one)

### Step 2: Add Remote and Push Code

```bash
cd /Users/jeffreyxu/Documents/lab/kafka/spring-kafka-integration

# Add GitHub remote (replace with your repository URL)
git remote add origin https://github.com/YOUR_USERNAME/spring-kafka-integration.git

# Push code to GitHub
git branch -M main
git push -u origin main
```

### Step 3: Configure GitHub Secrets

Go to your repository → Settings → Secrets and variables → Actions

Add these secrets:

#### Required Secrets:
- **`AWS_ACCESS_KEY_ID`**: Your AWS access key
- **`AWS_SECRET_ACCESS_KEY`**: Your AWS secret key
- **`KUBE_CONFIG`**: Base64 encoded kubeconfig file

#### To get the kubeconfig:
```bash
# On your Kubernetes master node
ssh -i ~/.ssh/my-ec2-key.pem ubuntu@52.90.236.10
sudo cat /etc/kubernetes/admin.conf | base64 -w 0
```

Copy the base64 output and add it as the `KUBE_CONFIG` secret.

### Step 4: Configure Additional Kubernetes Resources

The CI/CD pipeline expects these Kubernetes resources to exist:

#### Service Account
```bash
ssh -i ~/.ssh/my-ec2-key.pem ubuntu@52.90.236.10 "
kubectl create serviceaccount kafka-demo-sa -n kafka-demo --dry-run=client -o yaml | kubectl apply -f -
"
```

#### Database Secret
```bash
ssh -i ~/.ssh/my-ec2-key.pem ubuntu@52.90.236.10 "
kubectl create secret generic db-secret -n kafka-demo \
  --from-literal=username=kafka_user \
  --from-literal=password=kafka_password \
  --dry-run=client -o yaml | kubectl apply -f -
"
```

#### Application ConfigMap
```bash
ssh -i ~/.ssh/my-ec2-key.pem ubuntu@52.90.236.10 "
kubectl create configmap app-config -n kafka-demo \
  --from-literal=spring.kafka.bootstrap-servers=localhost:9092 \
  --from-literal=spring.datasource.url=jdbc:mysql://mysql-service:3306/kafka_demo \
  --dry-run=client -o yaml | kubectl apply -f -
"
```

## 🔄 CI/CD Pipeline Workflow

### Trigger Events:
- **Push to `main`**: Full pipeline (test → build → deploy)
- **Push to `develop`**: Test and build only
- **Pull Request**: Test only

### Pipeline Stages:

1. **Test Stage**:
   - Run unit tests
   - Generate test reports
   - Code coverage analysis

2. **Build & Push Stage** (main branch only):
   - Build Docker images
   - Push to AWS ECR
   - Tag with commit SHA and `latest`

3. **Deploy Stage** (main branch only):
   - Update Kubernetes manifests
   - Deploy to cluster
   - Verify deployment health

## 📊 Monitoring & Verification

### After Deployment:
1. **Check GitHub Actions**: Monitor pipeline execution
2. **Verify ECR Images**: Check AWS ECR for new images
3. **Monitor Kubernetes**: Check pod status and logs
4. **Test Applications**: Verify endpoints are accessible

### Useful Commands:
```bash
# Check pipeline status
# Go to: https://github.com/YOUR_USERNAME/spring-kafka-integration/actions

# Check ECR images
aws ecr describe-images --repository-name spring-kafka-producer --region us-east-1
aws ecr describe-images --repository-name spring-kafka-consumer --region us-east-1

# Check Kubernetes deployment
ssh -i ~/.ssh/my-ec2-key.pem ubuntu@52.90.236.10 "
  kubectl get pods -n kafka-demo
  kubectl get services -n kafka-demo
  kubectl logs -f deployment/spring-kafka-producer -n kafka-demo
  kubectl logs -f deployment/spring-kafka-consumer -n kafka-demo
"
```

## 🛠️ Customization Options

### Environment Variables:
- **AWS_REGION**: Change in `.github/workflows/ci-cd-pipeline.yml`
- **ECR_REPOSITORY_***: Update repository names if needed
- **Kubernetes namespace**: Update in deployment manifests

### Resource Limits:
- **CPU/Memory**: Adjust in `k8s/*/deployment.yaml`
- **Replicas**: Change replica count for scaling
- **Health checks**: Modify probe settings

## 🔧 Troubleshooting

### Common Issues:

1. **ECR Authentication Failed**:
   - Verify AWS credentials in GitHub secrets
   - Check IAM permissions for ECR access

2. **Kubernetes Deployment Failed**:
   - Verify kubeconfig is correct and base64 encoded
   - Check if required secrets and configmaps exist

3. **Image Pull Errors**:
   - Ensure ECR repositories exist
   - Verify image tags are correct

4. **Health Check Failures**:
   - Check application logs for startup issues
   - Verify database connectivity
   - Ensure Kafka is accessible

### Debug Commands:
```bash
# Check pipeline logs in GitHub Actions UI
# Check ECR repositories
aws ecr describe-repositories --region us-east-1

# Check Kubernetes resources
kubectl get all -n kafka-demo
kubectl describe pod POD_NAME -n kafka-demo
kubectl logs POD_NAME -n kafka-demo
```

## 🎯 Next Steps

1. **Set up GitHub repository** and push code
2. **Configure secrets** in GitHub repository settings
3. **Create Kubernetes resources** (service account, secrets, configmaps)
4. **Push to main branch** to trigger first deployment
5. **Monitor and verify** the deployment

The CI/CD pipeline will automatically:
- ✅ Test your code on every push
- ✅ Build and push Docker images to ECR
- ✅ Deploy to your Kubernetes cluster
- ✅ Verify deployment health
- ✅ Provide detailed logs and notifications

This gives you a production-ready, automated deployment pipeline for your Spring Boot + Kafka integration system!
