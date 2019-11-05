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


package com.luxoft.integration

import com.luxoft.poc.supplychain.IdentityInitService
import com.luxoft.poc.supplychain.data.AcceptanceResult
import com.luxoft.poc.supplychain.data.BusinessEntity
import com.luxoft.poc.supplychain.data.ChainOfAuthority
import com.luxoft.poc.supplychain.data.schema.DiagnosisDetails
import com.luxoft.poc.supplychain.data.schema.IndySchemaBuilder
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
import net.corda.testing.node.User
import java.time.Duration


class GradleDriven: e2eBase {

    val agentCert = CordaX500Name("SovrinAgent", "London", "GB")
    val issuerCert  = CordaX500Name("Manufacture", "London", "GB")
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

    override fun execute() {

        val user = User("user1", "test", permissions = setOf())

        val host = "localhost"

        val agent = CordaRPCClient(NetworkHostAndPort(host, 10202))
                .start(user.username, user.password).proxy

        val issuer = CordaRPCClient(NetworkHostAndPort(host, 10002))
                .start(user.username, user.password).proxy

        val treatment = CordaRPCClient(NetworkHostAndPort(host, 10102))
                .start(user.username, user.password).proxy

        val treatmentCenterIdentityService = IdentityInitService(treatment)
        treatmentCenterIdentityService.initTreatmentIndyMeta()
        treatmentCenterIdentityService.issueClaimTo(agentCert,
                diagnosisDetailsProposal,
                DiagnosisDetails.schemaName,
                DiagnosisDetails.schemaVersion)

        val issuerIdentityService = IdentityInitService(issuer)
        issuerIdentityService.assignPermissionsToMe(treatmentCert)
        issuerIdentityService.initIssuerIndyMeta()
        issuerIdentityService.issueClaimTo(agentCert,
                personalInfoProposal,
                PersonalInformation.schemaName,
                PersonalInformation.schemaVersion)


        val chainOfAuthority = ChainOfAuthority()
                .add(BusinessEntity.Treatment, treatmentCert)
                .add(BusinessEntity.Manufacturer, issuerCert)
                .add(BusinessEntity.Insuranse, treatmentCert)
                .add(BusinessEntity.Goverment, issuerCert)

        println("Indy step finished")

        val askNewPackageRes = agent.startFlowDynamic(AskNewPackage.Patient::class.java, chainOfAuthority)
        val packageId = askNewPackageRes.returnValue.getOrThrow(Duration.ofSeconds(30))

        println("Step 1 finished")

        val deliverShipmentRes = issuer.startFlowDynamic(DeliverShipment.Sender::class.java, packageId, treatmentCert)
        deliverShipmentRes.returnValue.getOrThrow(Duration.ofSeconds(30))

        println("Step 2 finished")

        val acceptanceRes = AcceptanceResult(packageId)
        val receiveShipmentRes = treatment.startFlowDynamic(ReceiveShipment.Receiver::class.java, acceptanceRes)
        receiveShipmentRes.returnValue.getOrThrow(Duration.ofSeconds(30))

        println("Step 3 finished")

        val packageWithdrawalRes = agent.startFlowDynamic(PackageWithdrawal.Owner::class.java, packageId)
        packageWithdrawalRes.returnValue.getOrThrow(Duration.ofSeconds(30))

        println("Step 4 finished")

        val packages = agent.vaultQueryBy<Package>().states.map { it.state.data.info }
        println("Packages: $packages")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            GradleDriven().execute()
        }
    }
}
*/
