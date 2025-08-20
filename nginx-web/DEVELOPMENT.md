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
The web console integrates with the main Kafka APIs using different access patterns:

**For Browser-Based JavaScript (External Access):**
- **Producer API**: `http://kafka.ciscloudlab.link/api/v1/messages/`
- **Consumer API**: `http://kafka.ciscloudlab.link/api/consumer/`
- **Health Checks**: `http://kafka.ciscloudlab.link/actuator/health`

**For Internal Pod-to-Pod Communication (Use CoreDNS):**
- **Producer Service**: `http://producer-service.kafka-demo.svc.cluster.local:8080`
- **Consumer Service**: `http://consumer-service.kafka-demo.svc.cluster.local:8080`
- **Nginx Web Service**: `http://nginx-web-console-service.kafka-demo.svc.cluster.local:80`

**Important Notes:**
- ✅ **Browser JavaScript**: Must use external URLs (`kafka.ciscloudlab.link`)
- ✅ **Server-Side/Pod Communication**: Use internal DNS (CoreDNS) for better performance
- ✅ **Nginx Proxy**: Can proxy internal calls to avoid CORS issues
- ❌ **Don't Mix**: Browser can't access internal DNS, pods shouldn't use external DNS for internal calls

## 🛠️ Development Workflow

### **GitHub-Only Development Process:**

**1. Edit Files Directly:**
```bash
# Navigate to nginx-web directory
cd nginx-web/

# Edit HTML files
vim html/index.html
vim html/dashboard.html

# Edit nginx configuration if needed
vim conf/nginx.conf
```

**2. Commit and Deploy:**
```bash
# Add changes
git add nginx-web/

# Commit with descriptive message
git commit -m "🌐 Update web console: [description of changes]"

# Push to trigger GitHub Actions pipeline
git push origin main
```

**3. Monitor Deployment:**
```bash
# Visit GitHub Actions to monitor build
# URL: https://github.com/Jeffrey-Xu/kafka/actions/workflows/nginx-web.yml

# Check deployment status
echo "Monitor the workflow progress in GitHub Actions"
echo "Build typically takes 3-5 minutes"
echo "Deployment verification included in pipeline"
```

**4. Test Changes:**
```bash
# Once pipeline completes, test the deployed application
curl -I http://kafka.ciscloudlab.link/web/
curl -I http://kafka.ciscloudlab.link/web/dashboard.html

# Or open in browser
open http://kafka.ciscloudlab.link/web/
```

### **Development Best Practices:**

**1. Small, Focused Changes:**
```bash
# Make one logical change per commit
git add nginx-web/html/index.html
git commit -m "🌐 Add new order validation form"

# Test in production after each deployment
# GitHub Actions provides full build and deploy
```

**2. Use GitHub Actions for All Testing:**
```bash
# The pipeline includes:
# - File validation
# - Docker build verification
# - Deployment testing
# - Internal connectivity checks
# - Ingress configuration validation

# No local testing needed - GitHub handles everything
```

**3. Monitor Pipeline Results:**
```bash
# Always check GitHub Actions results
# Pipeline will show:
# ✅ Build success/failure
# ✅ Deployment status
# ✅ Service connectivity
# ✅ Final access URLs
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

### **GitHub Actions Pipeline Issues:**

**1. Build Fails:**
```bash
# Check GitHub Actions logs for:
# - Dockerfile syntax errors
# - Missing files in nginx-web/
# - Docker Hub authentication issues

# Common fixes:
# - Verify all files exist in nginx-web/
# - Check Dockerfile syntax
# - Ensure DOCKER_HUB_TOKEN secret is set
```

**2. Deployment Fails:**
```bash
# Check GitHub Actions logs for:
# - Kubernetes authentication issues
# - Namespace access problems
# - Resource conflicts

# Common fixes:
# - Verify K8S_TOKEN secret is set
# - Check if kafka-demo namespace exists
# - Ensure no resource name conflicts
```

**3. Web App Not Accessible After Deployment:**
```bash
# GitHub Actions pipeline includes verification steps
# If pipeline succeeds but app not accessible:

# Check ingress status (via GitHub Actions logs)
# Verify service endpoints (shown in pipeline output)
# Wait 2-3 minutes for ingress propagation
```

### **Content Issues:**

**1. API Access Patterns:**
```bash
# ✅ CORRECT: Browser JavaScript (external access)
const API_BASE_URL = 'http://kafka.ciscloudlab.link';
fetch(`${API_BASE_URL}/api/v1/messages/user`, { ... });

# ✅ CORRECT: Internal pod-to-pod communication (use CoreDNS)
curl http://producer-service.kafka-demo.svc.cluster.local:8080/api/v1/messages/health

# ❌ WRONG: Don't use external DNS for internal calls
curl http://kafka.ciscloudlab.link/api/v1/messages/health  # Slower, unnecessary

# ❌ WRONG: Browser can't access internal DNS
fetch('http://producer-service.kafka-demo.svc.cluster.local:8080/api/v1/messages/user')
```

**2. API Calls Failing:**
```bash
# Ensure HTML files use correct API URLs:
# ✅ http://kafka.ciscloudlab.link/api/v1/messages/
# ✅ http://kafka.ciscloudlab.link/api/consumer/
# ❌ localhost or other domains

# GitHub Actions validates API URL references
```

**2. Styling/Layout Issues:**
```bash
# Edit CSS directly in HTML files
# Commit and push to see changes
# No local testing needed - use production for verification
```

### **Debug Information:**

**GitHub Actions provides complete debugging:**
- **Build Logs**: Docker build output and errors
- **Deployment Status**: Kubernetes deployment progress
- **Service Verification**: Internal connectivity tests
- **Final URLs**: Working access points

## 📝 Development Guidelines

### **API Access Best Practices:**
1. **Browser JavaScript**: Always use external URLs (`kafka.ciscloudlab.link`)
2. **Internal Services**: Use CoreDNS for pod-to-pod communication
3. **Performance**: Internal DNS is faster and doesn't go through load balancer
4. **Security**: Internal DNS keeps traffic within cluster
5. **CORS**: External URLs handle CORS properly for browser requests

### **File Editing:**
1. **HTML Files**: Edit `html/index.html` and `html/dashboard.html` directly
2. **Configuration**: Modify `conf/nginx.conf` if needed
3. **Docker**: Update `Dockerfile` for container changes
4. **API URLs**: Always use `kafka.ciscloudlab.link` domain

### **Commit Messages:**
```bash
# Use descriptive commit messages
git commit -m "🌐 Add order status tracking to dashboard"
git commit -m "🎨 Improve mobile responsive design"
git commit -m "🔧 Fix API error handling in order form"
git commit -m "📊 Add real-time statistics display"
```

### **Testing Strategy:**
1. **Commit Changes**: Push to trigger GitHub Actions
2. **Monitor Pipeline**: Watch build and deployment progress
3. **Verify Deployment**: Check provided URLs in pipeline output
4. **Test Functionality**: Use production environment for testing
5. **Iterate**: Make additional changes as needed

## 🔄 Integration with Main System

### **Service Dependencies:**
- **Producer Service**: For sending events via API
- **Consumer Service**: For monitoring processed events  
- **Ingress Controller**: For external access routing
- **Main Pipeline**: Shares same namespace and ingress

### **API Access Patterns:**
```
Browser → External DNS → Ingress → Services
  ↓
kafka.ciscloudlab.link/api/v1/messages/ → producer-service:8080

Pod → Internal DNS (CoreDNS) → Services  
  ↓
producer-service.kafka-demo.svc.cluster.local:8080
```

### **Data Flow:**
```
User → Web Console → Producer API → Kafka → Consumer → Database
                  ↓
              Dashboard ← Consumer API ← Consumer Service
```

### **Monitoring:**
- **GitHub Actions**: Complete build and deployment monitoring
- **Pipeline Verification**: Built-in connectivity and health checks
- **Production Testing**: Direct testing on deployed application

## 🚀 Quick Start

### **Make Your First Change:**
```bash
# 1. Edit a file
vim nginx-web/html/index.html

# 2. Commit and push
git add nginx-web/html/index.html
git commit -m "🌐 Update welcome message"
git push origin main

# 3. Monitor deployment
# Visit: https://github.com/Jeffrey-Xu/kafka/actions/workflows/nginx-web.yml

# 4. Test changes
# Visit: http://kafka.ciscloudlab.link/web/
```

### **Typical Development Cycle:**
1. **Edit** → nginx-web files
2. **Commit** → descriptive message
3. **Push** → triggers GitHub Actions
4. **Monitor** → pipeline progress
5. **Test** → production deployment
6. **Repeat** → for additional changes

---

**All development happens through GitHub Actions - no local setup required!**
