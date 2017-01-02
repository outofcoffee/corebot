#!/usr/bin/env bash
set -e

IMAGE_BASE_NAME="outofcoffee/corebot"
IMAGE_TAG="${1-dev}"
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

function buildImage()
{
    IMAGE_NAME="${IMAGE_BASE_NAME}:${IMAGE_TAG}"
    echo -e "\nBuilding Docker image: ${IMAGE_NAME}"

    cd $1
    docker build --tag ${IMAGE_NAME} .
}


function pushImage()
{
    echo -e "\nLogging in to Docker registry..."
    docker login --username "${DOCKER_USERNAME}" --password "${DOCKER_PASSWORD}" --email deprecated@example.com

    IMAGE_NAME="${IMAGE_BASE_NAME}:${IMAGE_TAG}"
    echo -e "\nPushing Docker image: ${IMAGE_NAME}"

    docker push ${IMAGE_NAME}
}

echo -e "\nBuilding image from local source"
buildImage "${SCRIPT_DIR}/../"

if [[ "dev" == "${IMAGE_TAG}" ]]; then
    echo -e "\nSkipped pushing dev image"
else
    pushImage
fi
