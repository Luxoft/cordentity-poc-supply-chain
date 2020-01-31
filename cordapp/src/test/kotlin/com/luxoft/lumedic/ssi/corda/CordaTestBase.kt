package com.luxoft.lumedic.ssi.corda

import com.luxoft.lumedic.ssi.corda.flow.AuthPatient
import net.corda.core.flows.FlowLogic
import net.corda.core.identity.CordaX500Name
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.internal.cordappsForPackages
import org.junit.After
import org.junit.Before
import org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY
import java.util.concurrent.CompletableFuture


/**
 * [CordaTestBase] is the base class for any test that uses mocked Corda network.
 *
 * Note: [projectReceiverFlows] must be kept updated!
 * */
open class CordaTestBase {

    protected lateinit var notary: StartedMockNode
    protected lateinit var lumedic: StartedMockNode

    /**
     * List of all flows that may be initiated by a message
     * */
    val projectReciverFlows = listOf(
        AuthPatient.AsyncProcess::class.java
    )

    /**
     * The mocked Corda network
     * */
    protected lateinit var net: MockNetwork

    protected val parties: MutableList<StartedMockNode> = mutableListOf()

    /**
     * Recreate nodes before each test
     *
     * Usage:
     *
     *     lateinit var issuer: TestStartedNode
     *
     *     @Before
     *     createNodes() {
     *         issuer = createPartyNode(CordaX500Name("Issuer", "London", "GB"))
     *     }
     * */
    protected fun createPartyNode(legalName: CordaX500Name): StartedMockNode {
        val party = net.createUnstartedNode(legalName).start()

        parties.add(party)

        for (flow in projectReciverFlows) {
            party.registerInitiatedFlow(flow)
        }
        party.services

        return party
    }

    @Before
    fun commonSetup() {
        System.setProperty(DEFAULT_LOG_LEVEL_KEY, "TRACE")

        val cordapps =
            cordappsForPackages(
                "com.luxoft.blockchainlab.corda.hyperledger.indy",
                "com.luxoft.lumedic.ssi.corda"
            ).map {
                it.withConfig(
                    mapOf(
                        "EpicBackend" to "http://localhost:8082"
                    )
                )
            }

        val networkParameters = MockNetworkParameters(
            cordappsForAllNodes = cordapps,
            networkParameters = testNetworkParameters(
                minimumPlatformVersion = 4
            )
        )
        net = MockNetwork(networkParameters)

        notary = net.defaultNotaryNode

        lumedic = createPartyNode(X500Name.Lumedic)
    }

    @After
    fun commonTearDown() {
        try {
            parties.clear()
        } finally {
            net.stopNodes()
        }
    }

    internal fun <T> StartedMockNode.runFlow(logic: FlowLogic<T>): CompletableFuture<T> {
        val future = startFlow(logic).toCompletableFuture()
        net.runNetwork()
        return future
    }

    fun CordaX500Name.getNodeByName() =
        net.defaultNotaryNode.services.identityService.wellKnownPartyFromX500Name(this)!!
}

fun StartedMockNode.getParty() = this.info.singleIdentity()

fun StartedMockNode.getName() = getParty().name

fun StartedMockNode.getPubKey() = getParty().owningKey
