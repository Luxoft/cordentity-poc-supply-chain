package com.luxoft.web.components

import net.corda.client.rpc.CordaRPCClient
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.NetworkHostAndPort.Companion.parse
import org.springframework.beans.factory.annotation.Value

import org.springframework.stereotype.Component


@Component
class RPCComponent {

    @Value("\${node.address}")
    private var nodeAddress: String = "localhost"

    @Value("\${node.rpcPort}")
    private var nodeRPCPort: Int = 10002

    @Value("\${node.rpcUser}")
    private var rpcUser: String = "user1"

    @Value("\${node.rpcPassword}")
    private var rpcPassword: String = "test"


    val services: CordaRPCOps by lazy {
        CordaRPCClient(NetworkHostAndPort(nodeAddress, nodeRPCPort))
                .start(rpcUser, rpcPassword)
                .proxy
    }

}