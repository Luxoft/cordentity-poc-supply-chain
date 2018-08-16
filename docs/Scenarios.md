### Package Supply Chain
<b>Decentralized package delivery with Self Sovereign Identity (SSI) layer.<b\>
<br/><br/>
Implements corda flows for a <u>Carrier</u>, or transport company, to transfer ownership of a package to a <u>Receiver</u>, or party that receives the package.

The system integrates a Corda supply chain to check issued claims using Hyperledger Indy verifiable claims. To request new product the client has to  provide cryptographic proofs to the Initiator. . .

##### Business roles
<u>User</u> - not a system component<br/>
<u>Client</u> - User who has permissions to request new package. (e.g Patient)  <br/>
<u>Initiator</u> - authority who checks client's SSI claims and initiates new production cycle. (e.g Treatment center) <br/>
<u>Issuer</u> - authority who creates new package, packs and delivers it back to Initiator. (e.g Laboratory) <br/>
<u>Carrier</u> - transportation company <br/>
<u>Receiver</u> - party finally receives package and gives it back to Client. Usually the same as a Initiator.<br/>

Client doesn't have Indy/Corda Node so his identity in the network is represented by Agent. Agent has a private Corda instance.

##### User Story

<b>Scenario#1</b>  New Package Request:

Preconditions:
1. Treatment Center page is opened
2. <b>Add Request</b> popup is opened
3. Client has mobile app to request new package
4. Client has an identity with Claims from Government and Insurer

User Story:
1. Client scans QR code and receives Treatment Center (TC) name
2. Client sends HTTP-API request to personal Agent Corda Node passing TC name
3. Agent Corda Node starts AskNewPackage flow between Agent & TC Corda Node
4. Agent Corda Node replies back to Client - "request in progress"
5. When all checks are completed a new Package request is created:
    1. Agent Corda Node receives new PackageRequest notification
    2. Agent Corda Node receives new Receipt claim from TC

<b>Scenario#2</b> Package Delivery
Preconditions:
1. Issuer Page is opened
2. Scenario#1 is passed
3. New Package request exist on Issuer page

User Story:
1. User presses <i>Manufacture & Send</i> on Issuer page opposite the request Package
2. Issuer sends HTTP-API request with <u>Package Serial</u> to Issuer Corda node to start a new delivery
3. Issuer corda node starts DeliverShipment flow with <u>Package Serial</u> & <u>Package Receiver name</u>

<b>Scenario#3</b> Delivery completed
Preconditions:
1. Treatment center Page is opened
2. Scenario#1 is passed
3. Scenario#2 is passed
3. Manufactured and sent package exists on Treatment center page

User Story:
1. User presses <i>Receive shipment</i> on Treatment center page opposite the Package
2. Treatment center sends HTTP-API request with <u>Package Serial</u> to Treatment center Corda node to receive shipment
3. Treatment center corda node starts ReceiveShipment flow with <u>Package Serial</u>

<b>Scenario#4</b> Delivery completed
Preconditions:
1. Treatment Center Page is opened
2. <b>Package collection</b> popup is opened
3. Scenario#1 is passed
4. Scenario#2 is passed
5. Scenario#3 is passed
5. Delivered package exists on Treatment center page

User Story:
1. Client scans QR code and receives serial number of package
2. Client sends HTTP-API request with <u>Package Serial</u> to Agent Corda node to accept package
3. Agent Corda node starts ReceiveShipment flow with <u>Package Serial</u>
4. Agent Corda Node replies back to Client - "identity verification in progress"
5. When all checks are completed and Package ownership transferred to Client Agent Corda Node receives Package transferring
notification

##### List of existing flows

There are 3 business processes in project:

* New Package Request <br/>
Every Client has individual set of Claims (in terms of Indy) received from widely known companies. <br/>
To request new product the Client has to convince the Initiator by providing cryptographic proofs. <br/>
The proofs can be built from number of independent private claims.<br/>

<i>For supply chain in Healthcare industry we assume that client gets Goverment and Insurance Claims, where <br/>
first provides Age, Nationality and other personal attributes, second one - Disease details</i>

Client requests new package from the Initiator. The Initiator verifies Client's claims and asks Issuer to create new package.
<br/> The following flows are running:

From     | To      | Interface | Flow                 | Subflow              | Comments                                            
---------|---------|-----------|----------------------|----------------------|----------------------------
Client   |Initiator| QR-Code   |                      |                      |Initiator shares Url to Initiator's validation peer
Client   |Agent    | HTTP-API  |                      |                      |Client invokes personal Agent and sends Receiver and Issuer CordaX500Names    
Agent    |Initiator| Corda P2P |<u>AskNewPackage</u>  |                      |Agent starts session with  Initiator
Initiator|Agent    | Corda P2P |<u>AskNewPackage</u>  |<u>VerifyClaimFlow</u>|Initiator asks and verifies Client's claims to produce new package
Initiator|Issuer   | Corda P2P |<u>AskNewPackage</u>  |<u>PrepareShipment</u>|Initiator creates new package request for Issuer
Initiator|Agent    | Corda P2P |<u>AskNewPackage</u>  |<u>IssueClaimFlow</u> |Initiator creates receipt as a IndyClaim for Agent

AskNewPackage internally calls <br/>
<i>(flows below are not exposed to RPC Clients)</i>

1. Indy Authentication Flow: <br/>

    From     | To      | Interface | SubFlow              |Comments                                                 
    ---------|---------|-----------|----------------------|-----------------------------------
    Initiator|Agent    | Corda P2P |<u>VerifyClaimFlow</u>|Initiator starts session with Agent
    Initiator|Agent    | Corda P2P |                      |Initiator sends ClaimProofRequest                    
    Agent    |Initiator| Corda P2P |                      |Agent generates and sends ClaimProof back            
    Initiator|         |           |                      |Initiator checks proof and returns results back           

2. Request New Package: <br/>

    From     | To      | Interface | SubFlow              | Comments                                                                    
    ---------|---------|-----------|----------------------|---------------------------------
    Initiator|Issuer   | Corda P2P |<u>PrepareShipment</u>|Initiator starts session with Issuer                  
    Issuer   |         |           |                      |Issuer creates new Package, collects signatures from Agent and Initiator
    Issuer   |         |           |                      |Issuer creates WayBill where puts all parties related to delivery       

3. Package confirmation/receipt: <br/>

    From     | To      | Interface | SubFlow             | Comments                                                                
    ---------|---------|-----------|---------------------|--------------------------------
    Initiator|Agent    | Corda P2P |<u>IssueClaimFlow</u>|Initiator starts session with Agent                
    Initiator|Agent    | Corda P2P |                     |Initiator gets Pairwise DID of Client                               
    Initiator|Agent    | Corda P2P |                     |Initiator creates ClaimOffer and sends it to Agent                 
    Agent    |Initiator| Corda P2P |                     |Agent creates ClaimRequest and sends it back                       
    Initiator|Agent    | Corda P2P |                     |Initiator creates Claim, collects signatures and send claim to Agent

* Package Delivery

When new package request created the Issuer starts production and delivery.<br/>
Product transferring is implemented in unified form and can be re-used as many time as needed.<br/>
Every new package transferring it's a new deal state (Shipment) between two parties.<br/>

<i>Currently production step is omitted and Issuer packs new package and runs delivery immediately.<i/>


From     | To      | Interface | Flow                 | Comments                                            
---------|---------|-----------|----------------------|-----------------------------------------------------
User     |Issuer   | HTTP-API  |<u>DeliverShipment</u>| Issuer selects the existing package request and creates new delivery to counterparty
User     |Receiver | HTTP-API  |<u>ReceiveShipment</u>| Counterparty gets new personal shipment and accept it manually


##### Client authentication

Only authorized and approved Clients can request new personal package producing. Each Client
