#!/usr/bin/env bash
set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DOCKERFILE_PATH="${SCRIPT_DIR}/../bots/slack-"
IMAGE_BASE_NAME="outofcoffee/corebot"
IMAGE_TAG="${1-dev}"
IMAGES=(
    "deploy"
    "items"
)

function buildImage()
{
    IMAGE_SUFFIX="$2"
    IMAGE_NAME="${IMAGE_BASE_NAME}${IMAGE_SUFFIX}:${IMAGE_TAG}"

    echo -e "\nBuilding Docker image: ${IMAGE_NAME}"
    cd $1
    docker build --tag ${IMAGE_NAME} .
}

function pushImage()
{
    IMAGE_SUFFIX="$1"
    IMAGE_NAME="${IMAGE_BASE_NAME}${IMAGE_SUFFIX}:${IMAGE_TAG}"

    echo -e "\nLogging in to Docker registry..."
    docker login --username "${DOCKER_USERNAME}" --password "${DOCKER_PASSWORD}" --email deprecated@example.com

    echo -e "\nPushing Docker image: ${IMAGE_NAME}"
    docker push ${IMAGE_NAME}
}

function buildPushImage()
{
    IMAGE_DIR="$1"
    echo -e "\nBuilding '${IMAGE_DIR}' image"

    if [[ "deploy" == "${IMAGE_DIR}" ]]; then
        IMAGE_SUFFIX=""
    else
        IMAGE_SUFFIX="-${IMAGE_DIR}"
    fi
    buildImage "${DOCKERFILE_PATH}${IMAGE_DIR}" ${IMAGE_SUFFIX}

    if [[ "dev" == "${IMAGE_TAG}" ]]; then
        echo -e "\nSkipped pushing dev image"
    else
        pushImage ${IMAGE_SUFFIX}
    fi
}

for IMAGE_DIR in "${IMAGES[@]}"; do
    buildPushImage ${IMAGE_DIR}
done
