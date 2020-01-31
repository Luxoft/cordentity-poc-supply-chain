package com.luxoft.lumedic.ssi.corda.service

import com.luxoft.blockchainlab.hyperledger.indy.models.ProofInfo
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import java.util.concurrent.LinkedBlockingQueue

@CordaService
class EpicCommunicationService(val serviceHub: AppServiceHub) : SingletonSerializeAsToken() {

    companion object {
        val submitInsurancePostSent = LinkedBlockingQueue<ProofInfo>()
    }

    private val epicEndpoint = serviceHub.getAppContext().config.getString("EpicBackend")

    fun submitInsurancePost(credentialProof: ProofInfo) {
        submitInsurancePostSent.put(credentialProof)
    }
}
