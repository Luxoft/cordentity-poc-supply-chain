Steps to set up remote environment:


#####ON LOCAL MACHINE:

Clean up the current docker state:

`docker rm -f $(docker ps -a -q)`

`docker system prune`

Provide IP-address of the machine to deploy Indy-Agents

`agents_ip=<YOUR_INDY_AGENTS_IP>`

Provide IP-address of the machine to deploy the backends and Cordapps

`monolith_ip=<YOUR_BACKENDS_IP>`

Edit the files manually or execute the following commands to fill the addresses into configuration 

`sed -i "/indyagents.*ansible_host=/s/=.*/=${agents_ip}/" devops/ansible/hosts`

`sed -i "/monolith.*ansible_host=/s/=.*/=${monolith_ip}/" devops/ansible/hosts`

`sed -i "/const\ val\ BASE_URL\ =\ \"http:\/\//s/localhost/${monolith_ip}/" SovrinAgentApp/app/src/main/java/com/luxoft/supplychain/sovrinagentapp/application/AppConfig.kt`

`sed -i "/const\ val\ WS_ENDPOINT\ =\ \"ws:\/\//s/localhost/${agents_ip}/" SovrinAgentApp/app/src/main/java/com/luxoft/supplychain/sovrinagentapp/application/AppConfig.kt`

The following is only needed when Agents IP and monolith IP are different

`sed -i "/indyuser\.agentWSEndpoint=/s/=.*/=ws:\/\/${agents_ip}:8096\/ws/" config/test/indyconfig/issuer.properties`

`sed -i "/indyuser\.agentWSEndpoint=/s/=.*/=ws:\/\/${agents_ip}:8095\/ws/" config/test/indyconfig/treatment.properties`

Build the project. Core artifacts are put in `/build` and `/webapp/build`

`./gradlew clean assemble`

Make sure your deployment key / credentials are specified in [all:vars] section in `devops/ansible/hosts` file:

`ansible_ssh_private_key_file=~/.ssh/your_private_key.pem`
`ansible_user=your_user_name` 

Run deployment scripts

`ansible-playbook -i devops/ansible/hosts devops/ansible/monolith.yaml`


SSH console to target machine

`ssh -i ~/.ssh/your_private_key.pem your_user_name@your_machine_ip` // IP from devops/ansible/hosts file



#####ON REMOTE MACHINE:

Start Corda nodes

`docker-compose up -d notary mfcorda tccorda`

Start web servers (after Corda nodes are properly up)

`docker-compose up -d tcweb mfweb`
