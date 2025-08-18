#!/bin/bash

# Local deployment script for Spring Boot + Kafka integration system
# This script builds Docker images locally and deploys to Kubernetes

set -e

echo "🚀 Starting local deployment of Spring Boot + Kafka integration system..."

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

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
print_status "Checking prerequisites..."

if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed or not running"
    exit 1
fi

if ! docker info &> /dev/null; then
    print_error "Docker daemon is not running"
    exit 1
fi

print_success "Prerequisites check passed"

# Build Docker images
print_status "Building Docker images..."

print_status "Building Producer image..."
docker build -f producer/Dockerfile -t $PRODUCER_IMAGE . || {
    print_error "Failed to build producer image"
    exit 1
}

print_status "Building Consumer image..."
docker build -f consumer/Dockerfile -t $CONSUMER_IMAGE . || {
    print_error "Failed to build consumer image"
    exit 1
}

print_success "Docker images built successfully"

# Save images to tar files for transfer
print_status "Saving images for transfer to Kubernetes cluster..."
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
    # Load images on master node
    sudo docker load < /tmp/producer-image.tar.gz
    sudo docker load < /tmp/consumer-image.tar.gz
    
    # Transfer and load on worker node
    scp -o StrictHostKeyChecking=no /tmp/producer-image.tar.gz ubuntu@worker01:/tmp/
    scp -o StrictHostKeyChecking=no /tmp/consumer-image.tar.gz ubuntu@worker01:/tmp/
    
    ssh -o StrictHostKeyChecking=no ubuntu@worker01 '
        sudo docker load < /tmp/producer-image.tar.gz
        sudo docker load < /tmp/consumer-image.tar.gz
        rm /tmp/producer-image.tar.gz /tmp/consumer-image.tar.gz
    '
    
    # Cleanup
    rm /tmp/producer-image.tar.gz /tmp/consumer-image.tar.gz
"

print_success "Images loaded on all nodes"

# Create temporary deployment files with local image names
print_status "Creating deployment manifests..."

# Create temporary directory for modified manifests
mkdir -p temp-k8s

# Update producer deployment
sed "s|IMAGE_PLACEHOLDER|$PRODUCER_IMAGE|g" k8s/producer/deployment.yaml > temp-k8s/producer-deployment.yaml
sed -i '' 's|imagePullPolicy: Always|imagePullPolicy: Never|g' temp-k8s/producer-deployment.yaml

# Update consumer deployment  
sed "s|IMAGE_PLACEHOLDER|$CONSUMER_IMAGE|g" k8s/consumer/deployment.yaml > temp-k8s/consumer-deployment.yaml
sed -i '' 's|imagePullPolicy: Always|imagePullPolicy: Never|g' temp-k8s/consumer-deployment.yaml

# Copy other manifests
cp k8s/ingress/ingress.yaml temp-k8s/

print_success "Deployment manifests created"

# Transfer manifests to cluster
print_status "Transferring manifests to cluster..."
scp -i $SSH_KEY -o StrictHostKeyChecking=no -r temp-k8s ubuntu@$K8S_HOST:/tmp/

# Deploy to Kubernetes
print_status "Deploying to Kubernetes..."

ssh -i $SSH_KEY -o StrictHostKeyChecking=no ubuntu@$K8S_HOST "
    # Apply deployments
    kubectl apply -f /tmp/temp-k8s/producer-deployment.yaml
    kubectl apply -f /tmp/temp-k8s/consumer-deployment.yaml
    kubectl apply -f /tmp/temp-k8s/ingress.yaml
    
    # Wait for deployments to be ready
    echo 'Waiting for Producer deployment...'
    kubectl rollout status deployment/spring-kafka-producer -n $NAMESPACE --timeout=300s
    
    echo 'Waiting for Consumer deployment...'
    kubectl rollout status deployment/spring-kafka-consumer -n $NAMESPACE --timeout=300s
    
    # Cleanup
    rm -rf /tmp/temp-k8s
"

print_success "Applications deployed to Kubernetes"

# Verify deployment
print_status "Verifying deployment..."

ssh -i $SSH_KEY -o StrictHostKeyChecking=no ubuntu@$K8S_HOST "
    echo '=== Pod Status ==='
    kubectl get pods -n $NAMESPACE
    
    echo -e '\n=== Service Status ==='
    kubectl get services -n $NAMESPACE
    
    echo -e '\n=== Ingress Status ==='
    kubectl get ingress -n $NAMESPACE
    
    echo -e '\n=== Deployment Status ==='
    kubectl get deployments -n $NAMESPACE
"

# Cleanup local files
rm -f producer-image.tar.gz consumer-image.tar.gz
rm -rf temp-k8s

print_success "Local deployment completed successfully!"

echo ""
echo "🎉 Deployment Summary:"
echo "   • Producer: http://kafka-demo.ciscloudlab.link/producer"
echo "   • Consumer: http://kafka-demo.ciscloudlab.link/consumer"
echo "   • Health checks available at /actuator/health endpoints"
echo "   • Consumer analytics at /api/consumer/stats"
echo ""
echo "📊 Next steps:"
echo "   1. Test the Producer API by sending events"
echo "   2. Monitor Consumer processing via analytics endpoints"
echo "   3. Check database for processed events"
echo "   4. Set up GitHub repository for automated CI/CD"
echo ""
