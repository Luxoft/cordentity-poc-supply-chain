# Supply chain POC API reference

This directory contains an api reference and other documentation to help you get familiar with the communication schema


## Business roles

1. **Patient** - person who have medical conditions sufficient to request a cure

2. **Treatment Center** - multiple medical centers that patient directly interacts with

3. **Manufacture** - entity producing medicines



## Corda nodes

0. **Notary** (docker container - notary) - service node validating and notarizing transactions across the network

1. **Sovrin Agent** (sacorda) - Patient's representative in Corda network. By current design it holds patient's wallet and is controlled by simple HTTP API. May change in future to move Indy SDK to client side (e.g. mobile phone)

2. **Treatment Center**( tccorda) - Treatment Center's Corda node.

3. **Manufacture** (mfcorda) - Manufacture's Corda node.



## Web applications

1. [Sovrin Agent](./sovrin_agent.md) (saweb) - web application that provides HTTP API for controlling Sovrin Agent node.

2. [Treatment Center](webapi/TreatmentCenter.md) (tcweb)- web application that serves Treatment Center's web page and provides HTTP API for controlling its Corda node.

3. [Manufacture](webapi/Manufacture.md) (mfweb) - web application that serves Manufacture's web page and provides HTTP API for controlling its Corda node.



## Mobile application(s)

1. **Patient** (folder - SovrinAgentApp) - Android app showing patient's claims and packages with their status. Allows scanning QR code to create package request or to pick-up medicine.
