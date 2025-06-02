#!/bin/bash

if [ -z "${REPOSITORY_PREFIX}" ]
then 
    echo "Please set the REPOSITORY_PREFIX environment variable"
    echo "Example: export REPOSITORY_PREFIX=myregistry/myuser"
    exit 1
else 
    echo "Deploying PetClinic services with repository prefix: ${REPOSITORY_PREFIX}"
    
    # Apply namespace and initial services
    echo "Creating namespace and initial services..."
    kubectl apply -f k8s/init-namespace/
    kubectl apply -f k8s/init-services/
    
    # Apply deployments with repository prefix substitution
    echo "Deploying microservices..."
    cat k8s/deployments/*.yaml | \
    sed "s#\${REPOSITORY_PREFIX}#${REPOSITORY_PREFIX}#g" | \
    kubectl apply -f -
    
    echo "Deployment complete! Check pod status with:"
    echo "kubectl get pods -n spring-petclinic"
fi