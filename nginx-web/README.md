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

### Automatic Deployment
Changes to any file in this folder will automatically trigger the nginx web console deployment.

### Manual Deployment
```bash
# Build and push image
docker build -t jeffreyxu2025/kafka:nginx-web-console-latest .
docker push jeffreyxu2025/kafka:nginx-web-console-latest

# Deploy to Kubernetes
kubectl apply -f ../k8s/nginx-web.yaml
```

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
