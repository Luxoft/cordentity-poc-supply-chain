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
 *//*


package com.luxoft.flow

import com.luxoft.blockchainlab.corda.hyperledger.indy.data.state.IndyCredential
import com.luxoft.blockchainlab.corda.hyperledger.indy.data.state.IndyCredentialProof
import com.luxoft.poc.supplychain.data.BusinessEntity
import com.luxoft.poc.supplychain.data.ChainOfAuthority
import com.luxoft.poc.supplychain.data.PackageState
import com.luxoft.poc.supplychain.data.schema.DiagnosisDetails
import com.luxoft.poc.supplychain.data.schema.IndySchemaBuilder
import com.luxoft.poc.supplychain.data.schema.PersonalInformation
import com.luxoft.poc.supplychain.data.state.Package
import com.luxoft.poc.supplychain.data.state.Shipment
import com.luxoft.poc.supplychain.data.state.getInfo
import com.luxoft.poc.supplychain.flow.medicine.AskNewPackage
import net.corda.core.flows.FlowException
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.Vault.StateStatus.ALL
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.internal.startFlow
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.Test
import kotlin.test.assertEquals

class AskForPackageFlowTests: IdentityBase(NetworkConfiguration()) {

    override fun onDown() = Unit
    override fun onUp() = Unit

    @Test
    fun `user not authorized to request new package`() {

        val meta = listOf<CredentialDesc>(
                CredentialDesc(
                        IndySchemaBuilder()
                                .addAttr(DiagnosisDetails.Attributes.Stage, "4")
                                .addAttr(DiagnosisDetails.Attributes.Disease, "leukemia")
                                .addAttr(DiagnosisDetails.Attributes.MedicineName, "package-name")
                                .addAttr(DiagnosisDetails.Attributes.Recommendation, "package-required")
                                .build(), getCredDefId(config.insurance, DiagnosisDetails), config.insurance),

                CredentialDesc(
                        IndySchemaBuilder()
                                .addAttr(PersonalInformation.Attributes.Age, "15")
                                .addAttr(PersonalInformation.Attributes.Nationality, "eu")
                                .addAttr(PersonalInformation.Attributes.Forename, "Mike J")
                                .build(), getCredDefId(config.goverment, PersonalInformation), config.goverment)
        )

        meta.forEach { issueClaim(it, config.agent) }

        val flowAskForPackage = AskNewPackage.Treatment(config.agent.getPartyDid())
        val askForPackageFuture = config.agent.services.startFlow(flowAskForPackage).resultFuture

        config.runNetwork()
        assertThatExceptionOfType(FlowException::class.java).isThrownBy {
            askForPackageFuture.getOrThrow()
        }
    }

    @Test
    fun  `user authorized to request new package`() {

        val meta = listOf<CredentialDesc>(
                CredentialDesc(
                        IndySchemaBuilder()
                                .addAttr(DiagnosisDetails.Attributes.Stage, "4")
                                .addAttr(DiagnosisDetails.Attributes.Disease, "leukemia")
                                .addAttr(DiagnosisDetails.Attributes.MedicineName, "package-name")
                                .addAttr(DiagnosisDetails.Attributes.Recommendation, "package-required")
                                .build(), getCredDefId(config.insurance, DiagnosisDetails), config.insurance),

                CredentialDesc(
                        IndySchemaBuilder()
                                .addAttr(PersonalInformation.Attributes.Age, "20")
                                .addAttr(PersonalInformation.Attributes.Nationality, "eu")
                                .addAttr(PersonalInformation.Attributes.Forename, "Mike J")
                                .build(), getCredDefId(config.goverment, PersonalInformation), config.goverment)
        )

        meta.forEach { issueClaim(it, config.agent) }

        val flowAskForPackage = AskNewPackage.Treatment(config.agent.getPartyDid())

        val askForPackageFuture = config.agent.services.startFlow(flowAskForPackage).resultFuture

        config.runNetwork()
        askForPackageFuture.getOrThrow()

        val criteria = QueryCriteria.VaultQueryCriteria(status = ALL)

        config.agent.database.transaction {
            val indyClaims = config.agent.services.vaultService.queryBy<IndyClaim>(criteria).states
            assertEquals(3, indyClaims.size)
            val claimsByIssuer = indyClaims.groupingBy { it.state.data.issuerDid }.eachCount()
            assertEquals(1, claimsByIssuer[config.insurance.getPartyDid()])
            assertEquals(1, claimsByIssuer[config.goverment.getPartyDid()])
            assertEquals(1, claimsByIssuer[config.treatment.getPartyDid()])

            val indyClaimProofs = config.agent.services.vaultService.queryBy<IndyClaimProof>(criteria).states
            assertEquals(1, indyClaimProofs.size)

            val packages = config.agent.services.vaultService.queryBy<Package>(criteria).states
            assertEquals(1, packages.size)
            assertEquals(PackageState.ISSUED, packages[0].getInfo().state)
            assertEquals(config.issuer.getName(),    packages[0].getInfo().issuedBy)
            assertEquals(config.treatment.getName(), packages[0].getInfo().requestedBy)

            assertEquals(0, config.agent.services.vaultService.queryBy<Shipment>(criteria).states.size)
        }

        config.treatment.database.transaction {
            // Claim is stored only on prover side
            assertEquals(1, config.treatment.services.vaultService.queryBy<IndyClaim>(criteria).states.size)
            // Proof is stored on both sides
            assertEquals(1, config.treatment.services.vaultService.queryBy<IndyClaimProof>(criteria).states.size)

            val packages = config.agent.services.vaultService.queryBy<Package>(criteria).states
            assertEquals(1, packages.size)
            assertEquals(PackageState.ISSUED, packages[0].getInfo().state)
            assertEquals(config.issuer.getName(),    packages[0].getInfo().issuedBy)
            assertEquals(config.treatment.getName(), packages[0].getInfo().requestedBy)

            assertEquals(0, config.treatment.services.vaultService.queryBy<Shipment>(criteria).states.size)
        }

        config.issuer.database.transaction {
            assertEquals(0, config.issuer.services.vaultService.queryBy<IndyClaim>(criteria).states.size)
            assertEquals(0, config.issuer.services.vaultService.queryBy<IndyClaimProof>(criteria).states.size)

            val packages = config.agent.services.vaultService.queryBy<Package>(criteria).states
            assertEquals(1, packages.size)
            assertEquals(PackageState.ISSUED, packages[0].getInfo().state)
            assertEquals(config.issuer.getName(),    packages[0].getInfo().issuedBy)
            assertEquals(config.treatment.getName(), packages[0].getInfo().requestedBy)

            assertEquals(0, config.issuer.services.vaultService.queryBy<Shipment>(criteria).states.size)
        }

        config.carrier.database.transaction {
            assertEquals(0, config.carrier.services.vaultService.queryBy<IndyClaim>(criteria).states.size)
            assertEquals(0, config.carrier.services.vaultService.queryBy<IndyClaimProof>(criteria).states.size)
            assertEquals(0, config.carrier.services.vaultService.queryBy<Package>(criteria).states.size)
            assertEquals(0, config.carrier.services.vaultService.queryBy<Shipment>(criteria).states.size)
        }
    }
}
*/
