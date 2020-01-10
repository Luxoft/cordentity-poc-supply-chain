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

package com.luxoft.lumedic.ssi.corda.service

import net.corda.core.flows.FlowLogic
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken

@CordaService
class EpicCommunicationService(serviceHub: AppServiceHub) : SingletonSerializeAsToken() {
    private val epicEndpoint = serviceHub.getAppContext().config.getString("EpicBackend")

    fun postData(body: String) {
//        TODO()
    }
}

fun FlowLogic<Any>.epicCommunicationService() = serviceHub.cordaService(EpicCommunicationService::class.java)
