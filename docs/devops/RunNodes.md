Steps to set up local environment:


Build the project. Core artifacts are put in `/build` and `/webapp/build`

`./gradlew clean assemble`


Start local Indy pool

`docker-compose up -d indypool`


Start Corda nodes.
Make sure you have xterm installed.

`./gradlew runNodes`


(Optional) In case of OOM - input in failed Corda terminal

`java -Xmx1024m -jar corda.jar`


Start web servers (after Corda nodes are properly up)
Make sure that ports 10001+ are free.

`docker-compose up -d tcweb mfweb saweb`
