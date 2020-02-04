package com.luxoft.blockchainlab.corda.hyperledger.indy.service

import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection
import com.luxoft.blockchainlab.hyperledger.indy.models.*
import com.nhaarman.mockito_kotlin.mock
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import org.mockito.Mockito.*
import rx.Single

@CordaService
class ConnectionService(services: AppServiceHub) : SingletonSerializeAsToken() {
    val indyPartyConnection = IndyPartyConnectionMock()

    fun sendCredentialOffer(offer: CredentialOffer, partyDID: String) =
        getPartyConnection(partyDID).sendCredentialOffer(offer)

    fun receiveCredentialOffer(partyDID: String) = getPartyConnection(partyDID).receiveCredentialOffer()

    fun sendCredentialRequest(request: CredentialRequestInfo, partyDID: String) =
        getPartyConnection(partyDID).sendCredentialRequest(request)

    fun receiveCredentialRequest(partyDID: String) = getPartyConnection(partyDID).receiveCredentialRequest()

    fun sendCredential(credential: CredentialInfo, partyDID: String) =
        getPartyConnection(partyDID).sendCredential(credential)

    fun receiveCredential(partyDID: String) = getPartyConnection(partyDID).receiveCredential()

    fun sendProofRequest(request: ProofRequest, partyDID: String) =
        getPartyConnection(partyDID).sendProofRequest(request)

    fun receiveProofRequest(partyDID: String) = getPartyConnection(partyDID).receiveProofRequest()

    fun sendProof(proof: ProofInfo, partyDID: String) = getPartyConnection(partyDID).sendProof(proof)

    fun receiveProof(partyDID: String) = getPartyConnection(partyDID).receiveProof()

    fun handleTailsRequestsWith(handler: (TailsRequest) -> TailsResponse, partyDID: String) =
        getPartyConnection(partyDID).handleTailsRequestsWith(handler)

    private fun getPartyConnection(partyDID: String) =
        indyPartyConnection

    private val connection = mock<AgentConnection>().apply {
        `when`(generateInvite())
            .thenReturn(Single.just("Stub invite"))
        `when`(waitForInvitedParty(anyString(), anyLong()))
            .thenReturn(Single.just(indyPartyConnection))
    }

    fun getConnection() = connection
}