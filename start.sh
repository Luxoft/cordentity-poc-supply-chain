#!/usr/bin/env bash

docker stop nervous_mahavira
docker start nervous_mahavira
docker-compose down
./gradlew cleanDefaultPool
./gradlew clean assemble
./gradlew runNodes
sleep 40
docker-compose build tcweb mfweb
docker-compose up -d tcweb mfweb