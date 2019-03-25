#!/usr/bin/env bash
set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DOCKERFILE_PATH="$( cd ${SCRIPT_DIR}/../bots/ && pwd )"
DOCKER_LOGIN_ARGS=""
IMAGE_REPOSITORY="outofcoffee/"
IMAGES=(
    "generic"
    "slack-deploy"
    "slack-items"
)

while getopts "e" OPT; do
    case ${OPT} in
        e) DOCKER_LOGIN_ARGS="--email dummy@example.com"
        ;;
    esac
done
shift $((OPTIND-1))

IMAGE_TAG="${1-dev}"

function getImageSuffixes() { case $1 in
    generic) echo "corebot-generic" ;;
    slack-deploy) echo "corebot corebot-slack-deploy" ;;
    slack-items) echo "corebot-slack-items" ;;
esac }

function buildImage()
{
    IMAGE_NAME="$1"
    IMAGE_PATH="$2"
    FULL_IMAGE_NAME="${IMAGE_REPOSITORY}${IMAGE_NAME}:${IMAGE_TAG}"

    echo -e "\nBuilding Docker image: ${IMAGE_NAME}"
    cd ${IMAGE_PATH}
    docker build --tag ${FULL_IMAGE_NAME} .
}

function pushImage()
{
    IMAGE_NAME="$1"
    FULL_IMAGE_NAME="${IMAGE_REPOSITORY}${IMAGE_NAME}:${IMAGE_TAG}"

    echo -e "\nPushing Docker image: ${IMAGE_NAME}"
    docker push ${FULL_IMAGE_NAME}
}

function buildPushImage()
{
    IMAGE_DIR="$1"
    echo -e "\nBuilding '${IMAGE_DIR}' image"

    for IMAGE_NAME in $( getImageSuffixes ${IMAGE_DIR} ); do
        buildImage ${IMAGE_NAME} "${DOCKERFILE_PATH}/${IMAGE_DIR}"

        if [[ "dev" == "${IMAGE_TAG}" ]]; then
            echo -e "\nSkipped pushing dev image"
        else
            pushImage ${IMAGE_NAME}
        fi
    done
}

function login() {
    if [[ "dev" == "${IMAGE_TAG}" ]]; then
        echo -e "\nSkipped registry login"
    else
        echo -e "\nLogging in to Docker registry..."
        docker login --username "${DOCKER_USERNAME}" --password "${DOCKER_PASSWORD}" ${DOCKER_LOGIN_ARGS}
    fi
}

login

for IMAGE_DIR in "${IMAGES[@]}"; do
    buildPushImage ${IMAGE_DIR}
done
