# Kubernetes Deployment Guide

## Prerequisites

- Minikube installed and running
- kubectl installed
- Docker image built: `ai-agent:dev`
- MCP HTTP wrapper running on host: http://localhost:3333

## Setup

### 1. Start Minikube

```bash
minikube start --cpus=2 --memory=4096
```

> **Note**: Adjust CPU and memory based on your Docker Desktop resources.
> If you have more resources available, you can increase these values.

### 2. Create Namespace

```bash
kubectl create namespace lab-agent
```

### 3. Build Docker Image in Minikube

Load the Docker image into Minikube's Docker daemon:

```bash
eval $(minikube -p minikube docker-env)
cd spring-agent
docker build -t ai-agent:dev .
cd ..
```

### 4. Create Secrets

Create a secret with your API keys and GitHub configuration:

```bash
kubectl -n lab-agent create secret generic agent-secrets \
  --from-literal=GOOGLE_AI_API_KEY="your-google-ai-key" \
  --from-literal=GITHUB_TOKEN="your-github-token" \
  --from-literal=GITHUB_OWNER="your-github-username" \
  --from-literal=GITHUB_REPO="lab_mcp_ai_agent_springboot"
```

Or using environment variables:

```bash
kubectl -n lab-agent create secret generic agent-secrets \
  --from-literal=GOOGLE_AI_API_KEY="$GOOGLE_AI_API_KEY" \
  --from-literal=GITHUB_TOKEN="$GITHUB_TOKEN" \
  --from-literal=GITHUB_OWNER="$GITHUB_OWNER" \
  --from-literal=GITHUB_REPO="$GITHUB_REPO"
```

### 5. Deploy the Application

```bash
kubectl apply -f k8s/ai-agent-deployment.yml
```

### 6. Verify Deployment

Check pods:

```bash
kubectl -n lab-agent get pods
```

Check service:

```bash
kubectl -n lab-agent get svc
```

View logs:

```bash
kubectl -n lab-agent logs -f deployment/ai-agent
```

### 7. Access the Application

Get the Minikube service URL:

```bash
minikube service ai-agent -n lab-agent --url
```

Or use port-forward:

```bash
kubectl -n lab-agent port-forward svc/ai-agent 8081:8081
```

Then access: http://localhost:8081

### 8. Test the Application

Create a user:

```bash
curl -X POST "http://localhost:8081/api/users?name=K8sTest&email=test@k8s.com"
```

## MCP Configuration

The deployment assumes the MCP HTTP wrapper is running on the host machine at `http://localhost:3333`.

The application connects to it via `http://host.minikube.internal:3333` which is automatically mapped by Minikube to the host machine.

Make sure the MCP wrapper is running:

```bash
cd mcp-github-http-wrapper
npm start
```

## Cleanup

Delete the deployment:

```bash
kubectl delete -f k8s/ai-agent-deployment.yml
```

Delete the secret:

```bash
kubectl -n lab-agent delete secret agent-secrets
```

Delete the namespace:

```bash
kubectl delete namespace lab-agent
```

Stop Minikube:

```bash
minikube stop
```

## Troubleshooting

### Pod not starting

```bash
kubectl -n lab-agent describe pod <pod-name>
kubectl -n lab-agent logs <pod-name>
```

### Image pull errors

Ensure you built the image inside Minikube's Docker:

```bash
eval $(minikube -p minikube docker-env)
docker images | grep ai-agent
```

### Connection to MCP fails

Check that the MCP wrapper is running on the host and accessible at http://localhost:3333

Test from within the pod:

```bash
kubectl -n lab-agent exec -it <pod-name> -- curl http://host.minikube.internal:3333/mcp
```
