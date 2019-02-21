#!/usr/bin/env bash

docker-compose down
./gradlew clean assemble
./gradlew runNodes
sleep 30
docker-compose build tcweb mfweb
docker-compose up -d tcweb mfweb