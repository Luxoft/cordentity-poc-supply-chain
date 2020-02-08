package com.luxoft.supplychain.sovrinagentapp.datasource.remote

import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection
import com.luxoft.blockchainlab.hyperledger.indy.models.ProofInfo
import com.luxoft.blockchainlab.hyperledger.indy.models.ProofRequest
import com.luxoft.blockchainlab.hyperledger.indy.utils.SerializationUtils
import com.luxoft.supplychain.sovrinagentapp.application.sharedPreferencesLastConnectionDiDKey
import com.luxoft.supplychain.sovrinagentapp.application.sharedPreferencesLastConnectionDiDName
import com.luxoft.supplychain.sovrinagentapp.application.sharedPreferencesRequstedDataKey
import com.luxoft.supplychain.sovrinagentapp.application.sharedPreferencesRequstedDataName
import com.luxoft.supplychain.sovrinagentapp.data.ApplicationState
import com.luxoft.supplychain.sovrinagentapp.data.Invite
import com.luxoft.supplychain.sovrinagentapp.data.SharedPreferencesStore
import com.luxoft.supplychain.sovrinagentapp.data.idatasource.RemoteDataSource
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class RemoteDataSourceImpl constructor(private val agentConnection: AgentConnection, private val applicationState: ApplicationState, private val sharedPreferencesStore: SharedPreferencesStore)
    : RemoteDataSource {

    override fun getCredentials(url: String): Single<String> {
        return Single.create<String> { s ->
            run {
                try {
                    agentConnection.acceptInvite(SerializationUtils.jSONToAny<Invite>(url).invite).toBlocking().value().apply {
                        //                        s.onNext("Receiving digital credential")
                        do {
                            val credOffer = try {
                                receiveCredentialOffer().timeout(5, TimeUnit.SECONDS).toBlocking().value()
                            } catch (e: RuntimeException) {
                                //End of waiting for new credentials
                                if (e.cause !is TimeoutException)
                                    throw e
                                null
                            }?.apply {
                                val indyUser = applicationState.indyState.indyUser.value!!
                                val credentialRequest = indyUser.createCredentialRequest(indyUser.walletUser.getIdentityDetails().did, this)
                                sendCredentialRequest(credentialRequest)
                                val credential = receiveCredential().toBlocking().value()
//                                s.onNext("Verifying digital credential")
                                indyUser.checkLedgerAndReceiveCredential(credential, credentialRequest, this)
                            }
                        } while (credOffer != null)
                        applicationState.updateWalletCredentials()
                        s.onSuccess("completed")
                    }
                } catch (er: Exception) {
                    s.onError(er)
                }
            }
        }
    }

    override fun sendProofOnRequest(url: String): Single<String> {
        return Single.create<String> { s ->
            run {
                try {
                    agentConnection.acceptInvite(SerializationUtils.jSONToAny<Invite>(url).invite).toBlocking().value().apply {
                        val proofRequest = receiveProofRequest().toBlocking().value()
                        val requestedData: Set<String> = proofRequest.requestedAttributes.keys + proofRequest.requestedPredicates.keys
                        val requestedDataStr = requestedData.joinToString(separator = ", ")
                        sharedPreferencesStore.writeString(sharedPreferencesRequstedDataName, sharedPreferencesRequstedDataKey, requestedDataStr)
                        val proofFromLedgerData: ProofInfo = applicationState.indyState.indyUser.value!!.createProofFromLedgerData(proofRequest)
                        sendProof(proofFromLedgerData)
                        s.onSuccess("completed")
                    }
                } catch (er: Exception) {
                    s.onError(er)
                }
            }
        }
    }

    override fun receiveProofRequest(url: String): Single<ProofRequest> {
        return Single.create<ProofRequest> { s ->
            run {
                try {
                    agentConnection.acceptInvite(SerializationUtils.jSONToAny<Invite>(url).invite).toBlocking().value().apply {
                        val proofRequest = receiveProofRequest().toBlocking().value()
                        sharedPreferencesStore.writeString(sharedPreferencesLastConnectionDiDName, sharedPreferencesLastConnectionDiDKey, this.partyDID())
                        s.onSuccess(proofRequest)
                    }
                } catch (er: Exception) {
                    s.onError(er)
                }
            }
        }
    }

    override fun sendProof(proofRequest: ProofRequest): Single<String> {
        return Single.create<String> { s ->
            run {
                try {
                    val requestedData: Set<String> = proofRequest.requestedAttributes.keys + proofRequest.requestedPredicates.keys
                    val requestedDataStr = requestedData.joinToString(separator = ", ")
                    sharedPreferencesStore.writeString(sharedPreferencesRequstedDataName, sharedPreferencesRequstedDataKey, requestedDataStr)
                    val did = sharedPreferencesStore.readString(sharedPreferencesLastConnectionDiDName, sharedPreferencesLastConnectionDiDKey)
                    val proofFromLedgerData: ProofInfo = applicationState.indyState.indyUser.value!!.createProofFromLedgerData(proofRequest)
                    agentConnection.getIndyPartyConnection(did!!).toBlocking().value().apply {
                        this!!.sendProof(proofFromLedgerData)
                        s.onSuccess("completed")
                    }
                } catch (er: Exception) {
                    s.onError(er)
                }
            }
        }
    }

}