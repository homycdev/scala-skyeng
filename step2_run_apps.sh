#!/usr/bin/env bash

set -e

# Set env variables
export DOCKER_TAG=local
export REGISTRY=registry.gitlab.io

docker-compose kill
docker-compose up -d --build

echo -e "\033[0;32m APPS ARE UP. \033[0m"
