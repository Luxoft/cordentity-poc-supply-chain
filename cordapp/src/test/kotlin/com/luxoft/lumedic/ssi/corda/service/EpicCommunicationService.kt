package com.luxoft.lumedic.ssi.corda.service

import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import java.util.concurrent.CompletableFuture

@CordaService
class EpicCommunicationService(val serviceHub: AppServiceHub) : SingletonSerializeAsToken() {

    companion object {
        val postSent = CompletableFuture<String>()
    }

    private val epicEndpoint = serviceHub.getAppContext().config.getString("EpicBackend")

    fun postData(body: String) {
        TODO()
    }
}
