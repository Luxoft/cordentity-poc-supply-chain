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

package com.luxoft.poc.supplychain

import SerializationUtils
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.CreateClaimDefFlow
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.CreateSchemaFlow
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.CordaSerializable
import net.corda.core.serialization.SingletonSerializeAsToken
import java.util.concurrent.ConcurrentHashMap

/**
 * A temporal substitute for a global Schema and Credential Definition discovery facility.
 *
 * Ideally a Schema catalog should be external to the system and available to all interested party.
 * Since Hyperledger has not yet provided such a facility,
 * [IndyArtifactsRegistry] should be enough for simple demos and POCs.
 * */
object IndyArtifactsRegistry {

    @CordaSerializable
    enum class ARTIFACT_TYPE { Schema, Definition }

    @CordaSerializable
    data class QueryRequest(val type: ARTIFACT_TYPE, val schemaName: String, val schemaVer: String)

    @CordaSerializable
    data class PutRequest(val type: ARTIFACT_TYPE, val payloadJson: String)

    @CordaSerializable
    data class CheckRequest(val type: ARTIFACT_TYPE, val filter: String)

    data class IndySchema(val name: String, val version: String, val attrs: List<String>) {
        fun filter(): String = name + version
    }
    data class IndyCredDef(val schemaid: String)

    fun FlowLogic<Any>.queryHandler(queryRequest: QueryRequest): String {
        try {
            val artifacts = serviceHub.cordaService(ArtifactsRegistry::class.java)

            val artifactId = when (queryRequest.type) {
                ARTIFACT_TYPE.Schema -> {
                    val filter = queryRequest.schemaName + queryRequest.schemaVer
                    artifacts.registry.get(filter)
                }
                ARTIFACT_TYPE.Definition -> {
                    val schemaId = queryHandler(queryRequest.copy(type = ARTIFACT_TYPE.Schema))
                    artifacts.registry.get(schemaId)
                }
            }

            requireNotNull(artifactId) {
                "Artifact wasnt found in registry: " +
                        "${queryRequest.type}, ${queryRequest.schemaName}, ${queryRequest.schemaVer}"
            }

            return artifactId!!

        } catch(t: Throwable) {
            logger.error("artifact query was failed " +
                    "${queryRequest.type}, ${queryRequest.schemaName}, ${queryRequest.schemaVer}", t)
            throw FlowException(t.message)
        }
    }

    fun FlowLogic<Any>.putHandler(putRequest: PutRequest): String {
        try {
            var artifactId: String
            val artifacts = serviceHub.cordaService(ArtifactsRegistry::class.java)

            when (putRequest.type) {
                ARTIFACT_TYPE.Schema -> {
                    val payload = SerializationUtils.jSONToAny<IndySchema>(putRequest.payloadJson)
                            ?: throw RuntimeException("Unable to parse schema from json")

                    artifactId = subFlow(CreateSchemaFlow.Authority(payload.name, payload.version, payload.attrs))
                    artifacts.put(payload.filter(), artifactId)
                }
                ARTIFACT_TYPE.Definition -> {
                    val payload = SerializationUtils.jSONToAny<IndyCredDef>(putRequest.payloadJson)
                            ?: throw RuntimeException("Unable to parse definition from json")

                    artifactId = subFlow(CreateClaimDefFlow.Authority(payload.schemaid))
                    artifacts.put(payload.schemaid, artifactId)
                }
                else -> throw FlowException("unknown indy artifact put request: " +
                        "${putRequest.type}, ${putRequest.payloadJson}")
            }
            return artifactId

        } catch(t: Throwable) {
            logger.error("artifact put was failed ${putRequest.type}, ${putRequest.payloadJson}", t)
            throw FlowException(t.message)
        }
    }

    fun FlowLogic<Any>.checkHandler(checkRequest: CheckRequest): Boolean {
        try {
            val artifacts = serviceHub.cordaService(ArtifactsRegistry::class.java)

            val isExist = when(checkRequest.type) {
                ARTIFACT_TYPE.Schema -> artifacts.registry.contains(checkRequest.filter)
                ARTIFACT_TYPE.Definition -> artifacts.registry.contains(checkRequest.filter)
                else -> throw FlowException("unknown indy artifact check request: " +
                        "${checkRequest.type}, ${checkRequest.filter}")
            }
            return isExist

        } catch(t: Throwable) {
            logger.error("artifact check was failed ${checkRequest.type}, ${checkRequest.filter}", t)
            throw FlowException(t.message)
        }
    }

    @CordaService
    class ArtifactsRegistry(services: AppServiceHub): SingletonSerializeAsToken() {
        val registry = ConcurrentHashMap<String, String>()
        fun put(filter: String, id: String) = registry.putIfAbsent(filter, id)
    }
}
