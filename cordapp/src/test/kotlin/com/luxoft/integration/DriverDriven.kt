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

//package com.luxoft.integration
//
//import com.luxoft.flow.IdentityBase
//import net.corda.client.rpc.CordaRPCClient
//import net.corda.core.identity.CordaX500Name
//import net.corda.core.internal.concurrent.transpose
//import net.corda.core.utilities.NetworkHostAndPort
//import net.corda.core.utilities.getOrThrow
//import net.corda.core.utilities.loggerFor
//import net.corda.testing.driver.DriverParameters
//
//import net.corda.testing.driver.driver
//import net.corda.testing.node.User
//import org.slf4j.Logger
//
//
//class DriverDriven: e2eBase, IdentityBase() {
//
//    private val logger: Logger = loggerFor<DriverDriven>()
//
//    override fun execute() {
//        val rpcUser = User("user1", "test", permissions = setOf())
//        val params = DriverParameters(
//                isDebug = true,
//                extraCordappPackagesToScan = listOf(
//                        "com.luxoft.poc.supplychain",
//                        "com.luxoft.blockchainlab.corda.hyperledger.indy")
//        )
//
//        val agentCert = CordaX500Name("Agent", "London", "GB")
//        val issuerCert  = CordaX500Name("Issuer", "London", "GB")
//        val treatmentCert = CordaX500Name("Treatment", "London", "GB")
//
//        val govermentCert = CordaX500Name("Goverment", "London", "GB")
//        val insuranceCert = CordaX500Name("Insurance", "London", "GB")
//
//        driver(params) {
//
//            val (agent, issuer, treatment, gov, insurance) = listOf(
//                    startNode(providedName = agentCert, rpcUsers = listOf(rpcUser)),
//                    startNode(providedName = issuerCert, rpcUsers = listOf(rpcUser)),
//                    startNode(providedName = treatmentCert, rpcUsers = listOf(rpcUser)),
//
//                    startNode(providedName = govermentCert, rpcUsers = listOf(rpcUser)),
//                    startNode(providedName = insuranceCert, rpcUsers = listOf(rpcUser))
//            ).transpose().getOrThrow()
//
//            initIndy(treatment.rpc)
//
////                corpA.rpc.startFlow(
////                        SupplychainFlow::Initiator,
////                        corpB.nodeInfo.legalIdentities.first()
////                ).returnValue.getOrThrow()
//
//
////                waitForAllNodesToFinish()
//        }
//    }
//
//    companion object {
//        @JvmStatic
//        fun main(args: Array<String>) {
//            DriverDriven().execute()
//        }
//    }
//
//}
