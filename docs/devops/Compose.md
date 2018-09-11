Steps to set up local environment:


Build artifacts

`./gradlew buildAndCopyFrontend`

`./gradlew webapp:jar`

`./gradlew deployCordaNetwork`


Start local Indy pool

`docker-compose up -d indypool`

Start Corda nodes

`docker-compose up -d notary mfcorda tccorda sacorda`

Start web servers (after Corda nodes are properly up)

Make sure that:
- webapp.jar is fresh in 'devops' folder (??)
- xterm is installed
- ports 10001+ are free

`docker-compose up -d tcweb mfweb saweb`
