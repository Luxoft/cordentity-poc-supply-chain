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

package com.luxoft.web.components

import com.luxoft.blockchainlab.corda.hyperledger.indy.data.state.IndySchema
import com.luxoft.poc.supplychain.IdentityInitService
import com.luxoft.poc.supplychain.data.AcceptanceResult
import com.luxoft.poc.supplychain.data.schema.DiagnosisDetails
import com.luxoft.poc.supplychain.data.schema.IndySchemaBuilder
import com.luxoft.poc.supplychain.data.schema.PackageReceipt
import com.luxoft.poc.supplychain.data.schema.PersonalInformation
import com.luxoft.poc.supplychain.data.state.Package
import com.luxoft.poc.supplychain.flow.DeliverShipment
import com.luxoft.poc.supplychain.flow.PackageWithdrawal
import com.luxoft.poc.supplychain.flow.ReceiveShipment
import com.luxoft.poc.supplychain.flow.medicine.AskNewPackage
import net.corda.client.rpc.CordaRPCClient
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import net.corda.nodeapi.internal.config.User
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.Duration
import javax.annotation.PostConstruct

@Component
@Profile("sovrinagent")
class IndyInitializer {
    private final val logger = loggerFor<IndyInitializer>()

    companion object {

        // TODO: How can we rewrite these names to make them look nice like "Marina Bay Hospital" etc???
        val agentCert = CordaX500Name("SovrinAgent", "London", "GB")
        val issuerCert = CordaX500Name("Manufacture", "London", "GB")
        val treatmentCert = CordaX500Name("TreatmentCenter", "London", "GB")

        val diagnosisDetailsProposal = IndySchemaBuilder()
                .addAttr(DiagnosisDetails.Attributes.Stage, "4")
                .addAttr(DiagnosisDetails.Attributes.Disease, "leukemia")
                .addAttr(DiagnosisDetails.Attributes.MedicineName, "package-name")
                .addAttr(DiagnosisDetails.Attributes.Recommendation, "package-required")
                .build()

        val personalInfoProposal = IndySchemaBuilder()
                .addAttr(PersonalInformation.Attributes.Age, "20")
                .addAttr(PersonalInformation.Attributes.Nationality, "eu")
                .addAttr(PersonalInformation.Attributes.Forename, "Mike J")
                .build()
    }

    @PostConstruct
    fun init() {
        val timeout = Duration.ofSeconds(1000L)

        val user = User("user1", "test", permissions = setOf())

        val agent = CordaRPCClient(NetworkHostAndPort("localhost", 10202))
                .start(user.username, user.password).proxy

        val issuer = CordaRPCClient(NetworkHostAndPort("localhost", 10002))
                .start(user.username, user.password).proxy

        val treatment = CordaRPCClient(NetworkHostAndPort("localhost", 10102))
                .start(user.username, user.password).proxy

        logger.info("Created all needed rpc clients")

        if (treatment.vaultQuery(IndySchema::class.java).states.isEmpty()) {
            val treatmentCenterIdentityService = IdentityInitService(treatment, timeout)
            treatmentCenterIdentityService.issueIndyMeta(PackageReceipt)

            logger.info("Treatment center indy stuff initialized")
        }

        logger.info("Creating test package...")

        val askNewPackageRes = agent.startFlowDynamic(AskNewPackage.Patient::class.java)
        val serial = askNewPackageRes.returnValue.getOrThrow(timeout)

        logger.info("1. Package request created")

        val deliverShipmentRes = issuer.startFlowDynamic(DeliverShipment.Sender::class.java, serial, treatmentCert)
        deliverShipmentRes.returnValue.getOrThrow(timeout)

        logger.info("2. Package request processed, package delivered pack")

        val acceptanceRes = AcceptanceResult(serial)
        val receiveShipmentRes = treatment.startFlowDynamic(ReceiveShipment.Receiver::class.java, acceptanceRes)
        receiveShipmentRes.returnValue.getOrThrow(timeout)

        logger.info("3. Package shipped")

        val packageWithdrawalRes = agent.startFlowDynamic(PackageWithdrawal.Owner::class.java, serial)
        packageWithdrawalRes.returnValue.getOrThrow(timeout)

        logger.info("4. Package withdrawal success")

        val packages = agent.vaultQueryBy<Package>().states.map { it.state.data.info }
        logger.info("Packages in vault: $packages")
        logger.info("Initialization passed")
    }
}
