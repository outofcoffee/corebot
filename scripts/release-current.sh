#!/usr/bin/env bash
set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT_DIR="${SCRIPT_DIR}/../"

cd ${ROOT_DIR}
CURRENT_VERSION="$( git describe --tags --exact-match )"

if [[ "${CURRENT_VERSION:0:1}" == "v" ]]; then
	CURRENT_VERSION="$( echo ${CURRENT_VERSION} | cut -c 2- )"
fi

read -p "Ready to release version ${CURRENT_VERSION} (y/N)?" CONFIRMATION
if [[ "y" != "${CONFIRMATION}" ]]; then
	exit 1
fi

echo "\nBuilding distribution"
cd ${ROOT_DIR}
./gradlew clean installdist

echo "\nPackaging and pushing"
cd ${SCRIPT_DIR}
./docker-build.sh ${CURRENT_VERSION}
