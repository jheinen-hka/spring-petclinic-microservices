#!/bin/bash

echo "Building Docker images for all PetClinic microservices..."

# Point to Minikube's Docker daemon
eval $(minikube -p minikube docker-env)

# Set repository prefix
REPOSITORY_PREFIX=${REPOSITORY_PREFIX:-petclinic}

echo "Using repository prefix: ${REPOSITORY_PREFIX}"

# Build all images
echo "Building API Gateway..."
./mvnw spring-boot:build-image -pl spring-petclinic-api-gateway \
  -Dspring-boot.build-image.imageName=${REPOSITORY_PREFIX}/spring-petclinic-api-gateway:latest \
  -q

echo "Building Customers Service..."
./mvnw spring-boot:build-image -pl spring-petclinic-customers-service \
  -Dspring-boot.build-image.imageName=${REPOSITORY_PREFIX}/spring-petclinic-customers-service:latest \
  -q

echo "Building Vets Service..."
./mvnw spring-boot:build-image -pl spring-petclinic-vets-service \
  -Dspring-boot.build-image.imageName=${REPOSITORY_PREFIX}/spring-petclinic-vets-service:latest \
  -q

echo "Building Visits Service..."
./mvnw spring-boot:build-image -pl spring-petclinic-visits-service \
  -Dspring-boot.build-image.imageName=${REPOSITORY_PREFIX}/spring-petclinic-visits-service:latest \
  -q

echo "Building Billing Service..."
./mvnw spring-boot:build-image -pl spring-petclinic-billing-service \
  -Dspring-boot.build-image.imageName=${REPOSITORY_PREFIX}/spring-petclinic-billing-service:latest \
  -q

echo "Building GenAI Service..."
./mvnw spring-boot:build-image -pl spring-petclinic-genai-service \
  -Dspring-boot.build-image.imageName=${REPOSITORY_PREFIX}/spring-petclinic-genai-service:latest \
  -q

echo "All images built successfully!"
echo "Listing built images:"
docker images | grep ${REPOSITORY_PREFIX}