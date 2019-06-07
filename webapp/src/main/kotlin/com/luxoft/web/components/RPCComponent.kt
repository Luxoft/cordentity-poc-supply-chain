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

import net.corda.client.rpc.CordaRPCClient
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component


@Component
@Profile("manufacture", "treatmentcenter")
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
