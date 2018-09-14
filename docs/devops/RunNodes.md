Steps to set up local environment:


Build artifacts and put them in `/build` and `/webapp/build`

`./gradlew clean assemble`


Start local Indy pool

`docker-compose up -d indypool`


Start Corda nodes

`./gradlew runNodes`


(Optional) In case of OOM - input in failed Corda terminal

`java -Xmx1024m -jar corda.jar`


Start web servers (after Corda nodes are properly up)

`docker-compose up -d tcweb mfweb saweb`
