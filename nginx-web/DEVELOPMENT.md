# Nginx Web Console Development Guide

## 🎯 Overview

The nginx-web console is a standalone web application that provides a user-friendly interface for interacting with the Kafka event-driven system. It's completely separated from the main producer/consumer applications with its own CI/CD pipeline.

## 🏗️ Architecture

```
nginx-web/
├── html/
│   ├── index.html          # Order form interface
│   └── dashboard.html      # Consumer dashboard
├── conf/
│   └── nginx.conf          # Nginx configuration
├── Dockerfile              # Container build instructions
└── DEVELOPMENT.md          # This file
```

## 🚀 Deployment Pipeline

### **Trigger Conditions:**
- Changes to `nginx-web/**` files
- Changes to `.github/workflows/nginx-web.yml`
- Manual workflow dispatch

### **Pipeline Flow:**
1. **Build Phase**: Creates Docker image `jeffreyxu2025/kafka:nginx-web-console-{commit}-{timestamp}`
2. **Deploy Phase**: Updates existing `nginx-web-console` deployment
3. **Verify Phase**: Tests internal connectivity and ingress configuration

### **Key Features:**
- ✅ **Independent Pipeline**: No impact on main Kafka application
- ✅ **Existing Deployment**: Uses current `nginx-web-console` service
- ✅ **Proper URLs**: Configured for `http://kafka.ciscloudlab.link/web/`
- ✅ **Same Credentials**: Uses main pipeline's Docker Hub and K8s tokens

## 🌐 Access URLs

### **Production URLs:**
- **Main App**: `http://kafka.ciscloudlab.link/web/`
- **Dashboard**: `http://kafka.ciscloudlab.link/web/dashboard.html`
- **Health Check**: `http://kafka.ciscloudlab.link/web/nginx-health`

### **API Integration:**
The web console integrates with the main Kafka APIs:
- **Producer API**: `http://kafka.ciscloudlab.link/api/v1/messages/`
- **Consumer API**: `http://kafka.ciscloudlab.link/api/consumer/`
- **Health Checks**: `http://kafka.ciscloudlab.link/actuator/health`

## 🛠️ Development Workflow

### **1. Local Development:**
```bash
# Navigate to nginx-web directory
cd nginx-web/

# Edit HTML files
vim html/index.html
vim html/dashboard.html

# Test locally with Docker
docker build -t nginx-web-local .
docker run -p 8080:8080 nginx-web-local
```

### **2. Testing Changes:**
```bash
# Open browser to test locally
open http://localhost:8080/

# Check nginx configuration
docker exec -it <container> nginx -t
```

### **3. Deployment:**
```bash
# Commit changes to trigger pipeline
git add nginx-web/
git commit -m "🌐 Update web console: [description]"
git push origin main

# Monitor deployment
# Visit: https://github.com/Jeffrey-Xu/kafka/actions/workflows/nginx-web.yml
```

## 📊 Current Configuration

### **Deployment Details:**
- **Name**: `nginx-web-console`
- **Namespace**: `kafka-demo`
- **Labels**: `app=nginx-web-console`, `component=web-frontend`
- **Service**: `nginx-web-console-service`
- **Port**: 80 → 8080

### **Ingress Configuration:**
- **Main Path**: `kafka.ciscloudlab.link/web` → `nginx-web-console-service:80`
- **Alternative**: `kafka-web.ciscloudlab.link/` → `nginx-web-console-service:80` (requires DNS)

### **Resource Limits:**
- **CPU**: 50m request, 100m limit
- **Memory**: 64Mi request, 128Mi limit
- **Replicas**: 1

## 🔧 Troubleshooting

### **Common Issues:**

**1. Pipeline Fails to Build:**
```bash
# Check Dockerfile syntax
docker build -t test nginx-web/

# Verify file permissions
ls -la nginx-web/html/
```

**2. Deployment Fails:**
```bash
# Check existing deployment
kubectl get deployment nginx-web-console -n kafka-demo

# Check service endpoints
kubectl get endpoints nginx-web-console-service -n kafka-demo
```

**3. Web App Not Accessible:**
```bash
# Test internal service
kubectl run test --rm -i --image=curlimages/curl --restart=Never -- \
  curl -s http://nginx-web-console-service.kafka-demo.svc.cluster.local/

# Check ingress configuration
kubectl describe ingress kafka-demo-ingress -n kafka-demo | grep -A5 "/web"
```

### **Debug Commands:**
```bash
# Check pod logs
kubectl logs -l app=nginx-web-console -n kafka-demo

# Check pod status
kubectl get pods -l app=nginx-web-console -n kafka-demo

# Test connectivity
kubectl exec -it <nginx-pod> -n kafka-demo -- curl localhost:8080/nginx-health
```

## 📝 Best Practices

### **Code Changes:**
1. **Test Locally First**: Always test changes with local Docker build
2. **Small Commits**: Make focused changes with clear commit messages
3. **API URLs**: Ensure all API calls use `kafka.ciscloudlab.link`
4. **Responsive Design**: Test on different screen sizes

### **HTML/CSS Guidelines:**
1. **Consistent Styling**: Follow existing CSS patterns
2. **Error Handling**: Include proper error messages for API failures
3. **Loading States**: Show loading indicators for async operations
4. **Accessibility**: Use semantic HTML and proper ARIA labels

### **JavaScript Best Practices:**
1. **API Integration**: Use proper error handling for fetch requests
2. **User Feedback**: Provide clear success/error messages
3. **Form Validation**: Validate inputs before API calls
4. **Performance**: Minimize API calls and cache when appropriate

## 🔄 Integration with Main System

### **Service Dependencies:**
- **Producer Service**: For sending events via API
- **Consumer Service**: For monitoring processed events
- **Ingress Controller**: For external access routing
- **Main Pipeline**: Shares same namespace and ingress

### **Data Flow:**
```
User → Web Console → Producer API → Kafka → Consumer → Database
                  ↓
              Dashboard ← Consumer API ← Consumer Service
```

### **Monitoring:**
- **Health Checks**: Built-in nginx health endpoint
- **API Status**: Real-time producer/consumer status checks
- **User Metrics**: Order submission counters and statistics

## 🚀 Future Enhancements

### **Planned Features:**
- Real-time event streaming dashboard
- Advanced filtering and search capabilities
- User authentication and authorization
- Mobile-responsive improvements
- Dark mode theme support

### **Technical Improvements:**
- WebSocket integration for real-time updates
- Progressive Web App (PWA) capabilities
- Enhanced error handling and retry logic
- Performance monitoring and analytics

---

**For questions or issues, check the GitHub Actions logs or contact the development team.**
