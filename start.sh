#!/usr/bin/env bash

docker-compose down --rmi local --volumes

set -e
./gradlew killCordaProcesses clean assemble bootstrap
docker-compose build cordabaseimage
docker-compose build notary tccorda mfcorda
docker-compose build tcweb mfweb
docker-compose up -d agent94 agent95 agent96 agentInitiator
docker-compose up -d notary tccorda mfcorda
sleep 30
docker-compose up -d tcweb mfweb
