# Supply chain POC API reference

This directory contains an api reference and other documentation to help you get familiar with the communication schema

## Nodes and roles
There are **four (?)** general nodes:
1. [Treatment Center](./treatment_center.md) (*TC*) - **as a role:** a multiple medical centers (placed in different countries) patient directly 
interacts with; **as a service:** a web application enabled to update shared state and manage patient's private data through
Indy SDK. Contains: `Corda node`, `Indy cordapp`, `Web server`
2. [Manufacture](./manufacture.md) (*MF*) - **as a role:** a place (some laboratory maybe) where cure is produced; **as a service:** a web 
application enabled to update shared state. Contains: `Corda node`, `Web server`
3. Courier (*CR*) - **as a role:** a courier is responsible for a cure delivery from *MF* to target *TC* (maybe passing 
customs); **as a service:** not implemented
4. Patient (*PA*) - **as a role:** a person who have medical conditions sufficient to request a cure; **as a service:** 
mobile app enabled to read shared state and manage patient's private data through Indy SDK via clouded service (agent). 
Contains: `Corda node`, `Push service`, `Indy cordapp`
