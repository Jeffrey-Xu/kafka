#!/bin/bash

echo "🌐 MANUAL NGINX WEB CONSOLE DEPLOYMENT"
echo "======================================"

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl not found. Please install kubectl first."
    exit 1
fi

# Check if we can connect to cluster
if ! kubectl cluster-info &> /dev/null; then
    echo "❌ Cannot connect to Kubernetes cluster"
    echo "Please ensure your kubeconfig is properly configured"
    exit 1
fi

echo "✅ kubectl configured and cluster accessible"

# Create namespace
echo "📦 Creating namespace..."
kubectl create namespace kafka-demo --dry-run=client -o yaml | kubectl apply -f -

# Deploy nginx web console
echo "🚀 Deploying nginx web console..."
kubectl apply -f - <<EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-web-console
  namespace: kafka-demo
  labels:
    app: nginx-web-console
    component: web-frontend
  annotations:
    date: "$(date +%s)"
spec:
  replicas: 1
  selector:
    matchLabels:
      app: nginx-web-console
  template:
    metadata:
      labels:
        app: nginx-web-console
        component: web-frontend
    spec:
      containers:
      - name: nginx
        image: jeffreyxu2025/kafka:nginx-web-console-latest
        ports:
        - containerPort: 80
          name: http
        resources:
          requests:
            memory: "64Mi"
            cpu: "50m"
          limits:
            memory: "128Mi"
            cpu: "100m"
        livenessProbe:
          httpGet:
            path: /nginx-health
            port: 80
          initialDelaySeconds: 10
          periodSeconds: 30
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /nginx-health
            port: 80
          initialDelaySeconds: 5
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 3
        env:
        - name: NGINX_HOST
          value: "localhost"
        - name: NGINX_PORT
          value: "80"
      restartPolicy: Always
---
apiVersion: v1
kind: Service
metadata:
  name: nginx-web-console-service
  namespace: kafka-demo
  labels:
    app: nginx-web-console
spec:
  selector:
    app: nginx-web-console
  ports:
  - name: http
    port: 80
    targetPort: 80
    protocol: TCP
  type: ClusterIP
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: nginx-web-console-ingress
  namespace: kafka-demo
  annotations:
    kubernetes.io/ingress.class: nginx
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "false"
    nginx.ingress.kubernetes.io/force-ssl-redirect: "false"
spec:
  rules:
  - host: kafka-web.ciscloudlab.link
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: nginx-web-console-service
            port:
              number: 80
EOF

# Wait for deployment
echo "⏳ Waiting for deployment to be ready..."
kubectl rollout status deployment/nginx-web-console -n kafka-demo --timeout=300s

# Show status
echo "✅ Nginx Web Console deployment completed!"
echo ""
echo "📊 Status:"
kubectl get pods -n kafka-demo -l app=nginx-web-console
kubectl get service nginx-web-console-service -n kafka-demo
kubectl get ingress nginx-web-console-ingress -n kafka-demo

echo ""
echo "🌐 Access URL: http://kafka-web.ciscloudlab.link/"
echo "📊 Dashboard: http://kafka-web.ciscloudlab.link/dashboard.html"
