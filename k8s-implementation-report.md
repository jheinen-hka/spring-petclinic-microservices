# Kubernetes Implementation Report - Spring PetClinic Microservices

## Overview

This document describes the implementation of the Spring PetClinic Microservices application on Kubernetes, migrating from a traditional Spring Cloud Netflix (Eureka) based service discovery to Kubernetes-native service discovery.

## Architecture Changes

### Original Architecture
- **Service Discovery**: Netflix Eureka Server
- **Configuration**: Spring Cloud Config Server
- **Load Balancing**: Ribbon with Eureka
- **API Gateway**: Spring Cloud Gateway with Eureka integration

### Kubernetes Architecture
- **Service Discovery**: Kubernetes DNS and Service resources
- **Configuration**: ConfigMaps and Secrets
- **Load Balancing**: Kubernetes Service load balancing
- **API Gateway**: Spring Cloud Gateway with static Kubernetes service URLs

## Implementation Steps

### 1. Kubernetes Resource Structure

Created a standardized directory structure for Kubernetes manifests:

```
k8s/
├── deployments/           # Deployment manifests for each service
├── helm-values/          # Helm values for stateful services (MySQL)
│   └── mysql-values.yaml
├── init-namespace/       # Namespace definition
│   └── 01-namespace.yaml
└── init-services/        # Service definitions and ConfigMaps
    ├── 02-config-map.yaml
    ├── 03-role.yaml
    ├── 04-api-gateway-service.yaml
    ├── 05-customers-service.yaml
    ├── 06-vets-service.yaml
    ├── 07-visits-service.yaml
    ├── 08-billing-service.yaml
    └── 09-genai-service.yaml
```

### 2. Service Discovery Migration

#### 2.1 Making Eureka Optional

Created conditional configuration classes for each service to enable/disable Eureka:

```java
@Configuration
@EnableDiscoveryClient
@ConditionalOnProperty(value = "eureka.client.enabled", matchIfMissing = true)
public class DiscoveryClientConfig {
}
```

This allows services to run with or without Eureka based on the deployment environment.

#### 2.2 Kubernetes Service URLs

In the Kubernetes environment, services communicate using DNS names following the pattern:
```
<service-name>.<namespace>.svc.cluster.local:<port>
```

Example service URLs:
- `customers-service.spring-petclinic.svc.cluster.local:8080`
- `vets-service.spring-petclinic.svc.cluster.local:8080`
- `billing-service.spring-petclinic.svc.cluster.local:8080`

### 3. Configuration Management

#### 3.1 ConfigMap

Created a central ConfigMap containing shared configuration:

```yaml
kind: ConfigMap
apiVersion: v1
metadata:
  name: petclinic-config
  namespace: spring-petclinic
data:
  application.yaml: |-
    # Shared configuration for all services
    spring:
      cloud:
        kubernetes:
          discovery:
            enabled: true
    # Service URLs for Feign clients
    customers-service-url: http://customers-service.spring-petclinic.svc.cluster.local:8080
    visits-service-url: http://visits-service.spring-petclinic.svc.cluster.local:8080
```

#### 3.2 Service-Specific Kubernetes Profiles

Created `application-kubernetes.yml` for each service with:
- Kubernetes-specific database connections
- Disabled Eureka client
- Health probe configuration
- Feign client URL overrides

Example for billing service:
```yaml
spring:
  datasource:
    url: jdbc:mysql://billing-db-mysql.spring-petclinic.svc.cluster.local:3306/service_instance_db
eureka:
  client:
    enabled: false
feign:
  client:
    config:
      customers-service:
        url: http://customers-service.spring-petclinic.svc.cluster.local:8080
```

### 4. Database Configuration

Using Helm to deploy MySQL instances with PersistentVolumes:

```yaml
auth:
  rootPassword: petclinic
  username: petclinic
  password: petclinic
  database: service_instance_db
primary:
  persistence:
    enabled: true
    size: 1Gi
```

Each service has its own MySQL instance:
- `customers-db-mysql`
- `vets-db-mysql`
- `visits-db-mysql`
- `billing-db-mysql`

### 5. API Gateway Routing

Updated Spring Cloud Gateway routes to use Kubernetes service URLs:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: customers-service
          uri: http://customers-service.spring-petclinic.svc.cluster.local:8080
          predicates:
            - Path=/api/customer/**
          filters:
            - StripPrefix=2
```

### 6. RBAC Configuration

Created Role and RoleBinding for services to query Kubernetes API:

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: namespace-reader
rules:
  - apiGroups: [""]
    resources: ["configmaps", "pods", "services", "endpoints", "secrets"]
    verbs: ["get", "list", "watch"]
```

## Dependencies Added

Added Spring Cloud Kubernetes to all services:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-kubernetes-client</artifactId>
</dependency>
```

## Services Included

1. **API Gateway** - External access point, routes requests to backend services
2. **Customers Service** - Manages pet owners and pets
3. **Vets Service** - Manages veterinarians
4. **Visits Service** - Manages pet visits
5. **Billing Service** - Handles billing (custom addition)
6. **GenAI Service** - AI-powered features (custom addition)

## Key Differences from Demo Implementation

1. **Additional Services**: Our implementation includes billing and GenAI services not present in the demo
2. **Conditional Eureka**: Services can still use Eureka in non-Kubernetes environments
3. **Feign Client Configuration**: Special handling for billing service's Feign clients to work with Kubernetes DNS

## Deployment Configuration

### 7. Kubernetes Deployments

Created deployment manifests for all services with consistent configuration:

- **Resource limits**: 1Gi memory, 1000m CPU requests
- **Health probes**: Liveness and readiness checks using Spring Boot Actuator
- **Graceful shutdown**: 10-second preStop hook
- **ConfigMap mounting**: All services mount the shared ConfigMap at `/etc/config`
- **Environment variables**: `SPRING_PROFILES_ACTIVE=kubernetes`
- **Database secrets**: Services reference their respective MySQL root passwords

Example deployment configuration:
```yaml
spec:
  replicas: 1
  template:
    spec:
      containers:
      - image: ${REPOSITORY_PREFIX}/spring-petclinic-customers-service:latest
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: kubernetes
        - name: MYSQL_ROOT_PASSWORD
          valueFrom:
             secretKeyRef:
               name: customers-db-mysql
               key: mysql-root-password
```

### 8. Deployment Scripts

Created automation scripts:

#### `scripts/setupDatabases.sh`
- Adds Bitnami Helm repository
- Installs MySQL instances for all services
- Uses shared Helm values for consistency

#### `scripts/deployToKubernetes.sh`
- Creates namespace and services
- Deploys all microservices
- Supports REPOSITORY_PREFIX environment variable

## Current Status

✅ **Completed**:
1. Kubernetes resource structure created
2. Service discovery migration from Eureka to k8s DNS
3. Configuration management with ConfigMaps
4. Database configuration with Helm
5. API Gateway routing updated
6. RBAC configuration
7. Deployment manifests for all services
8. Deployment automation scripts

🔄 **Next Steps**:
1. Build Docker images for all services
2. Deploy to Minikube for testing
3. Implement scaling experiments
4. Document self-healing behavior
5. Performance testing and optimization