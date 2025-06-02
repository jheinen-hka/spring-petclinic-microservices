#!/bin/bash

echo "Setting up MySQL databases for PetClinic services..."

# Add Bitnami Helm repository if not already added
helm repo add bitnami https://charts.bitnami.com/bitnami 2>/dev/null || true
helm repo update

# Install MySQL instances for each service
echo "Installing MySQL for customers service..."
helm install customers-db-mysql bitnami/mysql \
  --namespace spring-petclinic \
  --version 9.4.6 \
  --values k8s/helm-values/mysql-values.yaml

echo "Installing MySQL for vets service..."
helm install vets-db-mysql bitnami/mysql \
  --namespace spring-petclinic \
  --version 9.4.6 \
  --values k8s/helm-values/mysql-values.yaml

echo "Installing MySQL for visits service..."
helm install visits-db-mysql bitnami/mysql \
  --namespace spring-petclinic \
  --version 9.4.6 \
  --values k8s/helm-values/mysql-values.yaml

echo "Installing MySQL for billing service..."
helm install billing-db-mysql bitnami/mysql \
  --namespace spring-petclinic \
  --version 9.4.6 \
  --values k8s/helm-values/mysql-values.yaml

echo "Database setup complete! Check status with:"
echo "kubectl get pods -n spring-petclinic | grep mysql"