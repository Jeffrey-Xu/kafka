#!/bin/bash

# Simplified ARM64-compatible deployment script
# Uses default Docker buildx for cross-platform builds

set -e

echo "🍎 Starting simplified ARM64-compatible deployment..."

# Configuration
NAMESPACE="kafka-demo"
PRODUCER_IMAGE="spring-kafka-producer:local"
CONSUMER_IMAGE="spring-kafka-consumer:local"
K8S_HOST="52.90.236.10"
SSH_KEY="~/.ssh/my-ec2-key.pem"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
print_status "Checking prerequisites..."

if ! docker info &> /dev/null; then
    print_error "Docker daemon is not running"
    exit 1
fi

print_success "Docker is ready"

# Build Docker images with platform specification
print_status "Building Docker images for AMD64 platform..."

print_status "Building Producer image..."
docker buildx build \
    --platform linux/amd64 \
    -f producer/Dockerfile \
    -t $PRODUCER_IMAGE \
    --load \
    . || {
    print_error "Failed to build producer image"
    exit 1
}

print_status "Building Consumer image..."
docker buildx build \
    --platform linux/amd64 \
    -f consumer/Dockerfile \
    -t $CONSUMER_IMAGE \
    --load \
    . || {
    print_error "Failed to build consumer image"
    exit 1
}

print_success "Docker images built successfully"

# Save images to tar files for transfer
print_status "Saving images for transfer..."
docker save $PRODUCER_IMAGE | gzip > producer-image.tar.gz
docker save $CONSUMER_IMAGE | gzip > consumer-image.tar.gz

print_success "Images saved to tar files"

# Transfer images to Kubernetes cluster
print_status "Transferring images to Kubernetes cluster..."

scp -i $SSH_KEY -o StrictHostKeyChecking=no producer-image.tar.gz ubuntu@$K8S_HOST:/tmp/
scp -i $SSH_KEY -o StrictHostKeyChecking=no consumer-image.tar.gz ubuntu@$K8S_HOST:/tmp/

print_success "Images transferred to cluster"

# Load images on Kubernetes nodes
print_status "Loading images on Kubernetes nodes..."

ssh -i $SSH_KEY -o StrictHostKeyChecking=no ubuntu@$K8S_HOST "
    echo 'Loading images on master node...'
    sudo docker load < /tmp/producer-image.tar.gz
    sudo docker load < /tmp/consumer-image.tar.gz
    
    echo 'Transferring images to worker node...'
    scp -o StrictHostKeyChecking=no /tmp/producer-image.tar.gz ubuntu@worker01:/tmp/
    scp -o StrictHostKeyChecking=no /tmp/consumer-image.tar.gz ubuntu@worker01:/tmp/
    
    echo 'Loading images on worker node...'
    ssh -o StrictHostKeyChecking=no ubuntu@worker01 '
        sudo docker load < /tmp/producer-image.tar.gz
        sudo docker load < /tmp/consumer-image.tar.gz
        rm /tmp/producer-image.tar.gz /tmp/consumer-image.tar.gz
    '
    
    echo 'Cleaning up transfer files...'
    rm /tmp/producer-image.tar.gz /tmp/consumer-image.tar.gz
"

print_success "Images loaded on all nodes"

# Create Kubernetes resources if they don't exist
print_status "Ensuring Kubernetes resources exist..."

ssh -i $SSH_KEY -o StrictHostKeyChecking=no ubuntu@$K8S_HOST "
    # Create service account if it doesn't exist
    kubectl get serviceaccount kafka-demo-sa -n $NAMESPACE 2>/dev/null || \
    kubectl create serviceaccount kafka-demo-sa -n $NAMESPACE
    
    # Create database secret if it doesn't exist
    kubectl get secret db-secret -n $NAMESPACE 2>/dev/null || \
    kubectl create secret generic db-secret -n $NAMESPACE \
        --from-literal=username=kafka_user \
        --from-literal=password=kafka_password
    
    # Create app config if it doesn't exist
    kubectl get configmap app-config -n $NAMESPACE 2>/dev/null || \
    kubectl create configmap app-config -n $NAMESPACE \
        --from-literal=spring.kafka.bootstrap-servers=localhost:9092 \
        --from-literal=spring.datasource.url=jdbc:mysql://mysql-service:3306/kafka_demo
"

print_success "Kubernetes resources verified"

# Create temporary deployment files
print_status "Creating deployment manifests..."

mkdir -p temp-k8s

# Update producer deployment
sed "s|IMAGE_PLACEHOLDER|$PRODUCER_IMAGE|g" k8s/producer/deployment.yaml > temp-k8s/producer-deployment.yaml
sed -i '' 's|imagePullPolicy: Always|imagePullPolicy: Never|g' temp-k8s/producer-deployment.yaml

# Update consumer deployment  
sed "s|IMAGE_PLACEHOLDER|$CONSUMER_IMAGE|g" k8s/consumer/deployment.yaml > temp-k8s/consumer-deployment.yaml
sed -i '' 's|imagePullPolicy: Always|imagePullPolicy: Never|g' temp-k8s/consumer-deployment.yaml

# Copy ingress
cp k8s/ingress/ingress.yaml temp-k8s/

print_success "Deployment manifests created"

# Transfer manifests to cluster
print_status "Transferring manifests to cluster..."
scp -i $SSH_KEY -o StrictHostKeyChecking=no -r temp-k8s ubuntu@$K8S_HOST:/tmp/

# Deploy to Kubernetes
print_status "Deploying to Kubernetes..."

ssh -i $SSH_KEY -o StrictHostKeyChecking=no ubuntu@$K8S_HOST "
    echo 'Applying deployments...'
    kubectl apply -f /tmp/temp-k8s/producer-deployment.yaml
    kubectl apply -f /tmp/temp-k8s/consumer-deployment.yaml
    kubectl apply -f /tmp/temp-k8s/ingress.yaml
    
    echo 'Waiting for Producer deployment...'
    kubectl rollout status deployment/spring-kafka-producer -n $NAMESPACE --timeout=300s
    
    echo 'Waiting for Consumer deployment...'
    kubectl rollout status deployment/spring-kafka-consumer -n $NAMESPACE --timeout=300s
    
    rm -rf /tmp/temp-k8s
"

print_success "Applications deployed to Kubernetes"

# Verify deployment
print_status "Verifying deployment..."

ssh -i $SSH_KEY -o StrictHostKeyChecking=no ubuntu@$K8S_HOST "
    echo '=== Pod Status ==='
    kubectl get pods -n $NAMESPACE -o wide
    
    echo -e '\n=== Service Status ==='
    kubectl get services -n $NAMESPACE
    
    echo -e '\n=== Ingress Status ==='
    kubectl get ingress -n $NAMESPACE
"

# Cleanup
rm -f producer-image.tar.gz consumer-image.tar.gz
rm -rf temp-k8s

print_success "Deployment completed successfully!"

echo ""
echo "🎉 Your Spring Boot + Kafka system is now running!"
echo "   • Producer: http://kafka-demo.ciscloudlab.link/producer"
echo "   • Consumer: http://kafka-demo.ciscloudlab.link/consumer"
echo "   • Health: /actuator/health endpoints"
echo "   • Analytics: /api/consumer/stats"
echo ""
