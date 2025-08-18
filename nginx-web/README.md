# Nginx Web Console

This folder contains the dedicated web console for the Kafka Order Processing System.

## 🌐 Architecture

The nginx web console is completely separated from the producer/consumer applications:

- **Independent CI/CD**: Changes to this folder trigger only the `nginx-web.yml` workflow
- **Separate Docker Image**: `jeffreyxu2025/kafka:nginx-web-console-latest`
- **Dedicated Ingress**: `kafka-web.ciscloudlab.link`
- **API Proxy**: Nginx proxies API calls to producer/consumer services

## 📁 Structure

```
nginx-web/
├── html/
│   ├── index.html          # Order form interface
│   └── dashboard.html      # Consumer dashboard
├── conf/
│   └── nginx.conf          # Nginx configuration with API proxy
├── Dockerfile              # Multi-stage nginx build
└── README.md              # This file
```

## 🚀 Deployment

### Automatic Deployment (CI/CD)
Changes to any file in this folder will automatically trigger the nginx web console deployment.

**Note**: If kubectl connection fails in CI/CD, the Docker image will still be built and pushed. You can then use manual deployment.

### Manual Deployment
If automatic deployment fails or you want to deploy manually:

```bash
# Option 1: Use the deployment script
./nginx-web/deploy.sh

# Option 2: Manual kubectl commands
kubectl apply -f k8s/nginx-web.yaml

# Option 3: Build and deploy locally
docker build -t jeffreyxu2025/kafka:nginx-web-console-latest nginx-web/
docker push jeffreyxu2025/kafka:nginx-web-console-latest
kubectl apply -f k8s/nginx-web.yaml
```

### Troubleshooting Deployment
If the CI/CD deployment fails with kubectl errors:

1. **Check Docker Image**: The image should still be built and available at `jeffreyxu2025/kafka:nginx-web-console-latest`
2. **Manual Deploy**: Use `./nginx-web/deploy.sh` from a machine with kubectl access
3. **Verify Cluster**: Ensure the Kubernetes cluster is accessible and KUBECONFIG is properly configured

## 🌐 Access URLs

- **Order Form**: http://kafka-web.ciscloudlab.link/
- **Dashboard**: http://kafka-web.ciscloudlab.link/dashboard.html

## 🔧 Configuration

### API Endpoints
The nginx configuration proxies the following endpoints:
- `/api/` → Producer/Consumer APIs
- `/actuator/` → Producer health endpoints

### Environment Variables
- `NGINX_HOST`: localhost
- `NGINX_PORT`: 80

## 🎯 Features

### Order Form (`index.html`)
- Complete order submission form
- Real-time API integration
- Form validation and error handling
- Order confirmation and tracking

### Dashboard (`dashboard.html`)
- Real-time consumer statistics
- Order processing monitoring
- Auto-refresh every 5 seconds
- Responsive design

## 🔄 CI/CD Triggers

This nginx web console has independent CI/CD triggers:

### Triggers nginx-web.yml workflow:
- Changes to `nginx-web/**`
- Changes to `.github/workflows/nginx-web.yml`

### Does NOT trigger main CI/CD:
- Main application changes ignore this folder
- Complete separation of concerns
- Independent deployment cycles

## 🛠️ Development

To modify the web console:

1. Edit files in `nginx-web/html/` or `nginx-web/conf/`
2. Commit changes
3. Push to main branch
4. Nginx workflow automatically triggers
5. New image builds and deploys

## 📊 Monitoring

- Health check: `http://kafka-web.ciscloudlab.link/nginx-health`
- Kubernetes status: `kubectl get pods -n kafka-demo -l app=nginx-web-console`
- Logs: `kubectl logs -n kafka-demo -l app=nginx-web-console`
