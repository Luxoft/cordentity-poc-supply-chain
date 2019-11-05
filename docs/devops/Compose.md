Steps to set up local environment:


Build the project. Core artifacts are put in `/build` and `/webapp/build`

`./gradlew clean assemble`

Start local Indy pool

`docker-compose up -d indypool`

Start Corda nodes

`docker-compose up -d notary mfcorda tccorda`

Start web servers (after Corda nodes are properly up)
Make sure that ports 10001+ are free.

`docker-compose up -d tcweb mfweb`
