#!/bin/bash

CURRENT_PORT=$(grep -oP '(?<=:)\d+' /etc/nginx/proxy_pass.conf)

if [ "$CURRENT_PORT" == "8080" ]; then
  TARGET_PORT=8081
  TARGET_CONTAINER="mongle-green"
  OLD_CONTAINER="mongle-blue"
else
  TARGET_PORT=8080
  TARGET_CONTAINER="mongle-blue"
  OLD_CONTAINER="mongle-green"
fi

echo ">>> New Docker Image Pull"
docker pull $DOCKER_IMAGE_NAME:$IMAGE_TAG

echo ">>> Deploying to $TARGET_CONTAINER on port $TARGET_PORT"
export CONTAINER_NAME=$TARGET_CONTAINER
export PORT_MAPPING="$TARGET_PORT:8080"
docker compose --env-file .env up -d --no-deps app

echo ">>> Health check for new container..."
for i in {1..10}; do
  HEALTH_CHECK_RESPONSE=$(curl -s http://127.0.0.1:$TARGET_PORT/health)
  if echo "$HEALTH_CHECK_RESPONSE" | grep -q '"status":"UP"'; then
    echo ">>> Health check successful!"

    echo ">>> Switching Nginx proxy to port $TARGET_PORT"
    sudo sh -c "echo 'proxy_pass http://127.0.0.1:$TARGET_PORT;' > /etc/nginx/proxy_pass.conf"
    sudo nginx -s reload

    echo ">>> Stopping old container: $OLD_CONTAINER"

    docker stop $OLD_CONTAINER || true
    docker rm $OLD_CONTAINER || true

    exit 0
  fi
  echo "Health check failed. Retrying in 5 seconds... ($i/10)"
  sleep 5
done

echo "!!! Deployment failed: Health check timed out."
docker stop $TARGET_CONTAINER
docker rm $TARGET_CONTAINER
exit 1