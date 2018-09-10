# Personalized Health Care Supply Chain

Personalized Health Care Supply Chain connects patients and providers of medical services.
The system allows sharing private patients' information while providing extensive control over the usage of that information.


## Business Case

This project deals with a confined use case of creating personalised medicine and delivering them to a specific patient.
The connected parties in this case are Patients, Treatment Centers and Medicine Manufacturers.
The protected information may include results of medical tests, DNA information or personal information such as patient’s age or nationality.

For more information about usage scenarios please refer to [docs/Scenarios.md](docs/Scenarios.md).

### Business Roles

1. **Patient** - person who have medical conditions sufficient to request a cure

1. **Treatment Center** - multiple medical centers that patient directly interacts with

1. **Manufacture** - entity producing medicines


## Technical Solution

Personalized Health Care Supply Chain based on [Cordentity](https://github.com/Luxoft/cordentity), 
a bridge between [Hyperledger Indy Ledger](https://www.hyperledger.org/projects/hyperledger-indy) and [Corda Platform](https://www.corda.net/index.html).

For information about setup and installation please refer to [docs/devops/README.md](docs/devops/README.md).

### Components

1. **cordapp** - smart contracts, flows and other components that interact within the Corda network.

1. **frontend + webapp** - web applications for treatment centers, manufacturers and network administrators.

1. **SovrinAgentApp** - a mobile application for patients.

For more information about the project structure please refer to [docs/Actors.md](docs/Actors.md).


## Contributors

- [Alexander Kopnin](https://github.com/alkopnin)
- [Alexey Koren](https://github.com/alexeykoren)
- [Daniil Vodopian](https://github.com/voddan/)
