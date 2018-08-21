package com.luxoft.poc.supplychain.flow

import co.paralleluniverse.fibers.Suspendable
import com.luxoft.poc.supplychain.IndyArtifactsRegistry
import com.luxoft.poc.supplychain.IndyArtifactsRegistry.checkHandler
import com.luxoft.poc.supplychain.IndyArtifactsRegistry.putHandler
import com.luxoft.poc.supplychain.IndyArtifactsRegistry.queryHandler
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.unwrap

object ArtifactsManagement {

    @InitiatingFlow
    @StartableByRPC
    class Creator(private val artifactRequest: IndyArtifactsRegistry.PutRequest) : FlowLogic<String>() {
        @Suspendable override fun call(): String = putHandler(artifactRequest)
    }

    @InitiatingFlow
    @StartableByRPC
    class Accessor(private val artifactRequest: IndyArtifactsRegistry.QueryRequest) : FlowLogic<String>() {
        @Suspendable override fun call(): String = queryHandler(artifactRequest)
    }

    @InitiatingFlow
    @StartableByRPC
    class Verifier(private val artifactRequest: IndyArtifactsRegistry.CheckRequest) : FlowLogic<Boolean>() {
        @Suspendable override fun call(): Boolean = checkHandler(artifactRequest)
    }

    @InitiatingFlow
    class LocalCacheRefresher(private val artifactRequest: IndyArtifactsRegistry.QueryRequest,
                              private val ownerName: CordaX500Name) : FlowLogic<String>() {

        @Suspendable
        override fun call(): String {
            val owner = whoIs(ownerName)
            val flowSession: FlowSession = initiateFlow(owner)

            return flowSession.sendAndReceive<String>(artifactRequest).unwrap{ it }
        }
    }

    @InitiatedBy(LocalCacheRefresher::class)
    class ExternalAccessor(val flowSession: FlowSession): FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            flowSession.receive<IndyArtifactsRegistry.QueryRequest>().unwrap {
                flowSession.send(queryHandler(it))
            }
        }
    }
}