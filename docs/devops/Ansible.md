Steps to set up remote environment:


#####ON LOCAL MACHINE:

Build the project. Core artifacts are put in `/build` and `/webapp/build`

`./gradlew clean assemble`


Send stuff to remote server

`ansible-playbook -i devops/ansible/hosts devops/ansible/monolith.yaml`


Connect with teamblockchain ssh keys

`ssh -i <privatekeyfile> ubuntu@18.196.100.2` // or other IP from devops/ansible/hosts file



#####ON REMOTE MACHINE:

Start Indy pool

`docker-compose up -d indypool`

Start Corda nodes

`docker-compose up -d notary mfcorda tccorda sacorda`

Start web servers (after Corda nodes are properly up)

`docker-compose up -d tcweb mfweb saweb`
