#!/usr/bin/env bash

cd $(dirname $0)

./gradlew backendsComposeDown
./gradlew cordaComposeDown
./gradlew agentsComposeDown
./gradlew killCordaProcesses
./gradlew clean
docker system prune -f

set -e
./gradlew build
./gradlew baseimagesComposeBuild cordaComposeBuild backendsComposeBuild agentsComposeUp cordaComposeUp backendsComposeUp
