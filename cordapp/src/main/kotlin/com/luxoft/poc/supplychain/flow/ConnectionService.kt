package com.luxoft.poc.supplychain.flow

import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection
import com.luxoft.blockchainlab.corda.hyperledger.indy.Connection
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import java.lang.RuntimeException


@CordaService
class ConnectionService(val serviceHub: AppServiceHub) : SingletonSerializeAsToken() {
    private val connection: AgentConnection = AgentConnection("ws://10.255.255.21:8095/ws")
    val invite: AgentConnection.ReceiveInviteMessage by lazy { connection.genInvite() }

    fun getConnection(): Connection {
        if (connection.getCounterParty() == null)
            throw RuntimeException("Connection doesn't established yet")

        return connection
    }
}