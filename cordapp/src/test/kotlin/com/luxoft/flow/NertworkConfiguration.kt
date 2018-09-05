/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.luxoft.flow

import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.*
import com.luxoft.blockchainlab.corda.hyperledger.indy.service.IndyService
import com.luxoft.poc.supplychain.flow.*
import com.luxoft.poc.supplychain.flow.medicine.AskNewPackage
import com.natpryce.konfig.Configuration
import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.TestConfigurationsProvider
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.node.internal.StartedNode
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.node.internal.InternalMockNetwork
import net.corda.testing.node.internal.startFlow

import java.time.Duration

open class NetworkConfiguration {

    lateinit var net: InternalMockNetwork

    lateinit var treatment: StartedNode<InternalMockNetwork.MockNode>
    lateinit var issuer: StartedNode<InternalMockNetwork.MockNode>
    lateinit var carrier: StartedNode<InternalMockNetwork.MockNode>
    lateinit var agent: StartedNode<InternalMockNetwork.MockNode>
    lateinit var parties: List<StartedNode<InternalMockNetwork.MockNode>>

    lateinit var goverment: StartedNode<InternalMockNetwork.MockNode>
    lateinit var insurance: StartedNode<InternalMockNetwork.MockNode>

    fun up() {
        setupIndyConfigs()

        net = InternalMockNetwork(
                cordappPackages = listOf("com.luxoft.poc.supplychain", "com.luxoft.blockchainlab.corda.hyperledger.indy"),
                networkParameters = testNetworkParameters(maxTransactionSize = 10485760 * 3)
        )

        val agentCert = CordaX500Name("Agent", "London", "GB")
        val carrierCert = CordaX500Name("Carrier", "London", "GB")
        val issuerCert  = CordaX500Name("Laboratory", "London", "GB")
        val treatmentCert = CordaX500Name("Treatment", "London", "GB")

        val govermentCert = CordaX500Name("Goverment", "London", "GB")
        val insuranceCert = CordaX500Name("Insurance", "London", "GB")

        agent = net.createPartyNode(agentCert)
        issuer = net.createPartyNode(issuerCert)
        carrier = net.createPartyNode(carrierCert)
        treatment = net.createPartyNode(treatmentCert)

        goverment = net.createPartyNode(govermentCert)
        insurance = net.createPartyNode(insuranceCert)

        parties = listOf(issuer, treatment, carrier, agent, goverment, insurance)

        parties.forEach {
            it.registerInitiatedFlow(AskNewPackage.Treatment::class.java)

            it.registerInitiatedFlow(RequestForPackage.Counterparty::class.java)
            it.registerInitiatedFlow(DeliverShipment.Receiver::class.java)
            it.registerInitiatedFlow(ShipmentStatus.Observer::class.java)
            it.registerInitiatedFlow(ReceiveShipment.Counterparty::class.java)
            it.registerInitiatedFlow(Observers.Observer::class.java)

            it.registerInitiatedFlow(AssignPermissionsFlow.Authority::class.java)
            it.registerInitiatedFlow(CreatePairwiseFlow.Issuer::class.java)
            it.registerInitiatedFlow(IssueClaimFlow.Prover::class.java)
            it.registerInitiatedFlow(VerifyClaimFlow.Prover::class.java)
            it.registerInitiatedFlow(GetDidFlow.Authority::class.java)
        }

        parties.filter { it != goverment }.forEach { setPermissions(it, goverment) }
    }

    private fun setupIndyConfigs() {
        TestConfigurationsProvider.provider = object : TestConfigurationsProvider {
            override fun getConfig(name: String): Configuration? {
                return when(name) {
                    "Goverment" -> ConfigurationMap(mapOf(
                            "indyuser.walletName" to name,
                            "indyuser.role" to "trustee",
                            "indyuser.did" to "V4SGRU86Z58d6TV7PBUe6f",
                            "indyuser.seed" to "000000000000000000000000Trustee1"))
                    "Insurance" -> ConfigurationMap(mapOf(
                            "indyuser.walletName" to name + System.currentTimeMillis().toString(),
                            "indyuser.did" to "CzSfMVfq7U5pjTVtzd5uop",
                            "indyuser.seed" to "00000000000000000000000Insurance"))
                    "Treatment", "Laboratory", "Agent", "Carrier", "Notary Service" ->
                        ConfigurationMap(mapOf("indyuser.walletName" to name + System.currentTimeMillis().toString()))
                    else -> throw IllegalArgumentException("Unknown party: $name")

                }
            }
        }
    }

    private fun setPermissions(issuer: StartedNode<InternalMockNetwork.MockNode>,
                               authority: StartedNode<InternalMockNetwork.MockNode>) {
        val permissionsFuture = issuer.services.startFlow(
                AssignPermissionsFlow.Issuer(authority = authority.getName(), role = "TRUSTEE")
        ).resultFuture

        net.runNetwork()
        permissionsFuture.getOrThrow(Duration.ofSeconds(30))
    }

    fun down() {
        try {
            parties.forEach {
                it.services.cordaService(IndyService::class.java).indyUser.close()
            }
        } finally {
            net.stopNodes()
        }
    }

    fun runNetwork() = net.runNetwork()
}
