# Running Spring PetClinic Kubernetes Demo with Minikube

This guide provides a streamlined walkthrough for running the Spring PetClinic Cloud demo on Minikube, based on the [official documentation](https://github.com/spring-petclinic/spring-petclinic-cloud#compiling-and-pushing-to-kubernetes).

## Quick Start vs. Full Experience

- **Quick Start**: Use pre-built Docker Hub images (`springcommunity/*`)
- **Full Experience**: Build your own images to understand the containerization process and prepare for deploying custom services

## Prerequisites

- Docker Desktop installed and running
- Minikube installed (`brew install minikube` on macOS)
- kubectl installed (comes with Docker Desktop or `brew install kubectl`)
- Helm installed (`brew install helm` on macOS)
- Git installed

## Step 1: Start Docker Desktop

Ensure Docker Desktop is running. You can verify with:

```bash
docker version
```

## Step 2: Clone the Demo Repository
ireoka
Clone the Spring PetClinic Cloud repository to your local machine:

```bash
# Navigate to your desired parent directory
cd /Users/jvogt/git/hka/vsm-lab

# Clone the repository
git clone https://github.com/spring-petclinic/spring-petclinic-cloud.git
```

## Step 3: Start Minikube

Start Minikube with sufficient resources for running all microservices:

```bash
minikube start --memory=8192 --cpus=4
```

Wait for Minikube to fully start. You should see:
```
✅ Done! kubectl is now configured to use "minikube" cluster and "default" namespace by default
```

## Step 4: Build Docker Images (Optional)

The Spring PetClinic Cloud project provides pre-built images on Docker Hub under the `springcommunity` organization. You can choose to use these or build your own.

### Option A: Use Pre-built Public Images (Quick Start)

The deployment scripts will automatically use images from Docker Hub (e.g., `springcommunity/spring-petclinic-cloud-customers-service`). Skip to Step 5 if using this option.

### Option B: Build Your Own Images

For learning purposes or to test modifications, you can build images locally. The project uses Spring Boot 2.3+ with Cloud Native Buildpacks for optimized layering.

```bash
cd /Users/jvogt/git/hka/vsm-lab/spring-petclinic-cloud

# Set your Docker registry (use your Docker Hub username or registry URL)
export REPOSITORY_PREFIX=<your-dockerhub-username>

# Build all images using Paketo Buildpacks and push to registry
mvn spring-boot:build-image -Pk8s -DREPOSITORY_PREFIX=${REPOSITORY_PREFIX}

# Push images to your registry
./scripts/pushImages.sh
```

**Note for Minikube users**: Instead of pushing to a registry, you can build directly in Minikube's Docker daemon:
```bash
eval $(minikube -p minikube docker-env)
mvn spring-boot:build-image -Pk8s -DREPOSITORY_PREFIX=local
```

For detailed build instructions, see the [official README](https://github.com/spring-petclinic/spring-petclinic-cloud#compiling-and-pushing-to-kubernetes).

## Step 5: Create Kubernetes Namespace

Create the namespace where all PetClinic resources will be deployed:

```bash
kubectl create namespace spring-petclinic
```

Alternatively, you can use the namespace configuration from the repo:

```bash
kubectl apply -f /Users/jvogt/git/hka/vsm-lab/spring-petclinic-cloud/k8s/init-namespace/
```

## Step 6: Apply Initial Configurations

Apply all the initial service configurations, including ConfigMap, RBAC roles, and service definitions:

```bash
kubectl apply -f /Users/jvogt/git/hka/vsm-lab/spring-petclinic-cloud/k8s/init-services/
```

This creates:
- ConfigMap with application configuration
- RBAC roles for service discovery
- Service definitions for all microservices
- Wavefront proxy deployment (optional monitoring)

## Step 6: Install MySQL Databases with Helm

First, add the Bitnami Helm repository:

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
```

Install MySQL instances for each service:

```bash
# Install MySQL for vets service
helm install vets-db-mysql bitnami/mysql \
  --namespace spring-petclinic \
  --version 9.4.6 \
  --set auth.database=service_instance_db

# Install MySQL for visits service  
helm install visits-db-mysql bitnami/mysql \
  --namespace spring-petclinic \
  --version 9.4.6 \
  --set auth.database=service_instance_db

# Install MySQL for customers service
helm install customers-db-mysql bitnami/mysql \
  --namespace spring-petclinic \
  --version 9.4.6 \
  --set auth.database=service_instance_db
```

Wait for the MySQL pods to be ready:

```bash
kubectl get pods -n spring-petclinic -w
```

Press Ctrl+C when you see all MySQL pods are in `Running` state with `1/1` READY.

## Step 7: Deploy the Microservices

The project includes a deployment script that handles the REPOSITORY_PREFIX substitution. 

### Using Pre-built Images from Docker Hub

```bash
cd /Users/jvogt/git/hka/vsm-lab/spring-petclinic-cloud
export REPOSITORY_PREFIX=springcommunity
./scripts/deployToKubernetes.sh
```

### Using Your Own Images

```bash
cd /Users/jvogt/git/hka/vsm-lab/spring-petclinic-cloud
export REPOSITORY_PREFIX=<your-registry-prefix>
./scripts/deployToKubernetes.sh
```

### Alternative: Manual Deployment

If the script doesn't work or you want to understand what it does:

```bash
cat /Users/jvogt/git/hka/vsm-lab/spring-petclinic-cloud/k8s/*.yaml | \
  sed "s#\${REPOSITORY_PREFIX}#${REPOSITORY_PREFIX}#g" | \
  kubectl apply -f -
```

This deploys all services (api-gateway, customers-service, vets-service, visits-service) to the `spring-petclinic` namespace.

## Step 8: Monitor Deployment Progress

Watch the pods as they start up:

```bash
kubectl get pods -n spring-petclinic -w
```

Wait until all pods show `1/1` in the READY column and `Running` in the STATUS column. This may take several minutes as Docker images are pulled.

You can also check the status without watching:

```bash
kubectl get pods -n spring-petclinic
```

Expected output (all services should be Running):
```
NAME                                READY   STATUS    RESTARTS   AGE
api-gateway-66449bff9f-26zzm        1/1     Running   0          5m
customers-db-mysql-0                1/1     Running   0          7m
customers-service-7bc498fd8-65n6q   1/1     Running   0          5m
vets-db-mysql-0                     1/1     Running   0          7m
vets-service-5ff4fddd-zlj8n         1/1     Running   0          5m
visits-db-mysql-0                   1/1     Running   0          7m
visits-service-55f7bb4f84-kgfvn     1/1     Running   0          5m
```

Note: The wavefront-proxy pod may show errors, but this doesn't affect the core functionality.

## Step 9: Access the Application

Get the URL to access the PetClinic application:

```bash
minikube service api-gateway -n spring-petclinic --url
```

**Important**: Keep this terminal window open! On macOS with Docker driver, the tunnel needs to remain active.

The command will output a URL like:
```
http://127.0.0.1:63082
```

Open this URL in your web browser to access the Spring PetClinic application.

## Step 10: Verify Services

Check that all services are created:

```bash
kubectl get svc -n spring-petclinic
```

Expected services:
- api-gateway (LoadBalancer)
- customers-service (ClusterIP)
- vets-service (ClusterIP)
- visits-service (ClusterIP)
- MySQL services for each microservice

## Troubleshooting

### Check logs for any service
```bash
kubectl logs -n spring-petclinic <pod-name>
```

### Describe a pod for more details
```bash
kubectl describe pod -n spring-petclinic <pod-name>
```

### If pods are stuck in ContainerCreating
This usually means images are still being pulled. Check events:
```bash
kubectl get events -n spring-petclinic --sort-by='.lastTimestamp'
```

### Clean up and start over
If you need to start fresh:
```bash
# Delete the namespace (this removes everything)
kubectl delete namespace spring-petclinic

# Stop Minikube
minikube stop

# Start again from Step 3
```

## Next Steps

Once the demo is running, you can:
- Explore the application at the provided URL
- Scale services: `kubectl scale deployment <service-name> --replicas=3 -n spring-petclinic`
- Check service discovery by examining how services communicate
- Review the Kubernetes configurations in the `/k8s` directory

## Architecture Notes

This demo showcases:
- **Kubernetes-native service discovery** instead of Eureka
- **ConfigMaps** for configuration instead of Spring Cloud Config Server
- **Helm** for managing stateful applications (MySQL)
- **Spring Cloud Kubernetes** integration for service discovery
- **LoadBalancer** service type for external access (simulated by Minikube tunnel)
