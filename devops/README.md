Steps to set up local environment:


Build artifacts

`./gradlew deployNodes`

`./gradlew deployStuffToNodes`


Start local Indy pool

`docker-compose up -d indypool`

Start Corda nodes

`docker-compose up -d notary mfcorda tccorda sacorda`

Start web servers (after Corda nodes are properly up)
Make sure webapp.jar is fresh in 'devops' folder

`docker-compose up -d tcweb mfweb saweb`
