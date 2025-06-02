# Spring PetClinic Microservices - Kubernetes Deployment

This directory contains Kubernetes manifests and scripts for deploying the Spring PetClinic Microservices application on Kubernetes.

## Architecture

The deployment replaces the traditional Spring Cloud Netflix stack with Kubernetes-native features:
- **Service Discovery**: Kubernetes DNS instead of Eureka
- **Configuration**: ConfigMaps instead of Config Server
- **Load Balancing**: Kubernetes Services instead of Ribbon
- **External Access**: LoadBalancer service for API Gateway

## Directory Structure

```
k8s/
├── deployments/          # Deployment manifests for each microservice
├── helm-values/         # Helm values for MySQL databases
├── init-namespace/      # Namespace definition
└── init-services/       # Service definitions and ConfigMap
```

## Prerequisites

- Kubernetes cluster (Minikube, Docker Desktop, or cloud provider)
- kubectl configured
- Helm 3.x installed
- Docker images built (see parent README)

## Quick Start

### 1. Build Docker Images

```bash
# From the project root
./mvnw clean install -P buildDocker
```

### 2. Start Minikube (if using locally)

```bash
minikube start --memory=8192 --cpus=4 -p minikube-petclinic
```

### 3. Deploy the Application

```bash
# Set your Docker repository prefix
export REPOSITORY_PREFIX=springcommunity  # or your registry

# Deploy infrastructure and services
./scripts/deployToKubernetes.sh

# Setup MySQL databases
./scripts/setupDatabases.sh
```

### 4. Access the Application

```bash
# For Minikube
minikube service api-gateway -n spring-petclinic --url -p minikube-petclinic

# For other clusters, get the LoadBalancer IP
kubectl get svc api-gateway -n spring-petclinic
```

## Services Deployed

| Service | Type | Port | Description |
|---------|------|------|-------------|
| api-gateway | LoadBalancer | 80 | External entry point, routes to backend services |
| customers-service | ClusterIP | 8080 | Manages owners and pets |
| vets-service | ClusterIP | 8080 | Manages veterinarians |
| visits-service | ClusterIP | 8080 | Manages pet visits |
| billing-service | ClusterIP | 8080 | Handles billing (custom) |
| genai-service | ClusterIP | 8080 | AI features (custom) |

## Databases

Each service has its own MySQL instance deployed via Helm:
- customers-db-mysql
- vets-db-mysql
- visits-db-mysql
- billing-db-mysql

## Configuration

Shared configuration is stored in the `petclinic-config` ConfigMap. Service-specific configurations use the `kubernetes` Spring profile.

## Troubleshooting

### Image Pull Issues

For custom services not in public registries:

```bash
# Load image into Minikube
minikube image load <image-name> -p minikube-petclinic

# Update deployment to not pull
kubectl patch deployment <service-name> -n spring-petclinic \
  -p '{"spec":{"template":{"spec":{"containers":[{"name":"<service-name>","imagePullPolicy":"Never"}]}}}}'
```

### Check Pod Status

```bash
kubectl get pods -n spring-petclinic
kubectl describe pod <pod-name> -n spring-petclinic
kubectl logs <pod-name> -n spring-petclinic
```

### Database Connection Issues

Verify MySQL secrets:
```bash
kubectl get secrets -n spring-petclinic
kubectl get secret <service>-db-mysql -n spring-petclinic -o yaml
```

## Scaling

To scale a service:
```bash
kubectl scale deployment <service-name> --replicas=3 -n spring-petclinic
```

## Cleanup

To remove the entire deployment:
```bash
kubectl delete namespace spring-petclinic
```