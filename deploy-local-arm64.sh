#!/bin/bash

# ARM64-compatible deployment script for Spring Boot + Kafka integration system
# Builds multi-platform Docker images on Apple Silicon and deploys to AMD64 Kubernetes cluster

set -e

echo "🍎 Starting ARM64-compatible deployment of Spring Boot + Kafka integration system..."

# Configuration
NAMESPACE="kafka-demo"
PRODUCER_IMAGE="spring-kafka-producer:local"
CONSUMER_IMAGE="spring-kafka-consumer:local"
K8S_HOST="52.90.236.10"
SSH_KEY="~/.ssh/my-ec2-key.pem"
TARGET_PLATFORM="linux/amd64"  # Target platform for Kubernetes cluster

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
print_status "Checking prerequisites for ARM64 Mac..."

if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed"
    exit 1
fi

if ! docker info &> /dev/null; then
    print_error "Docker daemon is not running. Please start Docker Desktop."
    exit 1
fi

# Check if buildx is available (required for multi-platform builds)
if ! docker buildx version &> /dev/null; then
    print_error "Docker buildx is not available. Please update Docker Desktop."
    exit 1
fi

# Check current architecture
CURRENT_ARCH=$(uname -m)
print_status "Current architecture: $CURRENT_ARCH"
print_status "Target platform: $TARGET_PLATFORM"

print_success "Prerequisites check passed"

# Create buildx builder for multi-platform builds
print_status "Setting up multi-platform builder..."

# Remove existing builder if it exists
docker buildx rm multiplatform-builder 2>/dev/null || true

# Create new builder
docker buildx create --name multiplatform-builder --driver docker-container --bootstrap
docker buildx use multiplatform-builder

# Verify builder supports target platform
if ! docker buildx inspect --bootstrap | grep -q "linux/amd64"; then
    print_error "Builder doesn't support linux/amd64 platform"
    exit 1
fi

print_success "Multi-platform builder configured"

# Build Docker images for AMD64 platform
print_status "Building Docker images for AMD64 platform..."

print_status "Building Producer image for $TARGET_PLATFORM..."
docker buildx build \
    --platform $TARGET_PLATFORM \
    -f producer/Dockerfile \
    -t $PRODUCER_IMAGE \
    --load \
    . || {
    print_error "Failed to build producer image"
    exit 1
}

print_status "Building Consumer image for $TARGET_PLATFORM..."
docker buildx build \
    --platform $TARGET_PLATFORM \
    -f consumer/Dockerfile \
    -t $CONSUMER_IMAGE \
    --load \
    . || {
    print_error "Failed to build consumer image"
    exit 1
}

print_success "Docker images built successfully for AMD64"

# Verify image architecture
print_status "Verifying image architectures..."
PRODUCER_ARCH=$(docker inspect $PRODUCER_IMAGE | jq -r '.[0].Architecture')
CONSUMER_ARCH=$(docker inspect $CONSUMER_IMAGE | jq -r '.[0].Architecture')

print_status "Producer image architecture: $PRODUCER_ARCH"
print_status "Consumer image architecture: $CONSUMER_ARCH"

if [[ "$PRODUCER_ARCH" != "amd64" ]] || [[ "$CONSUMER_ARCH" != "amd64" ]]; then
    print_warning "Images may not be AMD64. This might cause issues on the cluster."
fi

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
    
    echo 'Verifying images on both nodes...'
    echo 'Master node images:'
    sudo docker images | grep spring-kafka
    echo 'Worker node images:'
    ssh -o StrictHostKeyChecking=no ubuntu@worker01 'sudo docker images | grep spring-kafka'
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
    echo 'Applying deployments...'
    kubectl apply -f /tmp/temp-k8s/producer-deployment.yaml
    kubectl apply -f /tmp/temp-k8s/consumer-deployment.yaml
    kubectl apply -f /tmp/temp-k8s/ingress.yaml
    
    echo 'Waiting for Producer deployment...'
    kubectl rollout status deployment/spring-kafka-producer -n $NAMESPACE --timeout=300s
    
    echo 'Waiting for Consumer deployment...'
    kubectl rollout status deployment/spring-kafka-consumer -n $NAMESPACE --timeout=300s
    
    echo 'Cleaning up temporary files...'
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
    
    echo -e '\n=== Deployment Status ==='
    kubectl get deployments -n $NAMESPACE
    
    echo -e '\n=== Recent Pod Events ==='
    kubectl get events -n $NAMESPACE --sort-by='.lastTimestamp' | tail -10
"

# Test application health
print_status "Testing application health..."

ssh -i $SSH_KEY -o StrictHostKeyChecking=no ubuntu@$K8S_HOST "
    echo 'Testing Producer health...'
    kubectl exec deployment/spring-kafka-producer -n $NAMESPACE -- curl -f http://localhost:8080/actuator/health 2>/dev/null && echo 'Producer: ✅ Healthy' || echo 'Producer: ❌ Not ready'
    
    echo 'Testing Consumer health...'
    kubectl exec deployment/spring-kafka-consumer -n $NAMESPACE -- curl -f http://localhost:8081/api/consumer/health 2>/dev/null && echo 'Consumer: ✅ Healthy' || echo 'Consumer: ❌ Not ready'
"

# Cleanup local files
print_status "Cleaning up local files..."
rm -f producer-image.tar.gz consumer-image.tar.gz
rm -rf temp-k8s

# Reset Docker buildx to default
docker buildx use default
docker buildx rm multiplatform-builder 2>/dev/null || true

print_success "ARM64-compatible deployment completed successfully!"

echo ""
echo "🎉 Deployment Summary:"
echo "   • Architecture: Built AMD64 images on ARM64 Mac"
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
echo "🔧 Troubleshooting:"
echo "   • Check pod logs: kubectl logs -f deployment/spring-kafka-producer -n kafka-demo"
echo "   • Check events: kubectl get events -n kafka-demo --sort-by='.lastTimestamp'"
echo "   • Test connectivity: kubectl exec -it deployment/mysql -n kafka-demo -- mysql -u root -prootpassword"
echo ""
