#!/bin/bash

# Push images to Minikube registry
REGISTRY="${REGISTRY:-localhost:50267}"
echo "Using registry: $REGISTRY"

# List of services to push
services=(
  "api-gateway"
  "customers-service"
  "vets-service"
  "visits-service"
  "billing-service"
  "genai-service"
)

# Tag and push each service
for service in "${services[@]}"; do
  source_image="springcommunity/spring-petclinic-${service}:latest"
  target_image="${REGISTRY}/spring-petclinic-${service}:latest"
  
  echo "Tagging ${source_image} as ${target_image}..."
  docker tag "${source_image}" "${target_image}"
  
  echo "Pushing ${target_image}..."
  docker push "${target_image}"
  echo "---"
done

echo "All images pushed to registry!"