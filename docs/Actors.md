# Supply chain POC API reference

This directory contains an api reference and other documentation to help you get familiar with the communication schema


## Business roles

1. **Patient** - person who has medical conditions sufficient to request a cure

2. **Treatment Center** - multiple medical centers that patient directly interacts with

3. **Manufacture** - entity producing medicine



## Corda nodes

1. **Notary** (docker container - notary) - service node validating and notarizing transactions across the network

2. **Treatment Center**( tccorda) - Treatment Center's Corda node.

3. **Manufacture** (mfcorda) - Manufacture's Corda node.



## Web applications

1. [Treatment Center](webapi/TreatmentCenter.md) (tcweb)- web application that serves Treatment Center's web page and provides HTTP API for controlling its Corda node.

2. [Manufacture](webapi/Manufacture.md) (mfweb) - web application that serves Manufacture's web page and provides HTTP API for controlling its Corda node.



## Mobile application(s)

1. **Patient** (folder - SovrinAgentApp) - Android app showing patient's claims and packages with their status. Allows scanning QR code to create package request or to pick-up medicine.
