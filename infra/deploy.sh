#!/bin/bash

# Exit if any command fails
set -e

echo "Starting deployment..."

echo "Pulling latest code..."
git pull origin main

echo "Building and starting containers..."
docker-compose -f docker-compose.prod.yaml up --build -d

echo "Running Django migrations..."
docker exec swe573-api-1 python3 manage.py migrate

# Optional: collect static files
# docker exec <your_api_container_name_or_id> python3 manage.py collectstatic --noinput

echo "Deployment finished!"