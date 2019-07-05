#!/usr/bin/env bash
set -e

docker-compose down --rmi local --volumes
./gradlew killCordaProcesses clean assemble
./gradlew runNodes
#docker-compose up -d tccorda mfcorda notary
sleep 30
docker-compose up -d agent94 agent95 agent96 agentInitiator
docker-compose build tcweb mfweb
docker-compose up -d tcweb mfweb