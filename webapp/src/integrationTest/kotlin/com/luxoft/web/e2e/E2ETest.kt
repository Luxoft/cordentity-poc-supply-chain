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

package com.luxoft.web.e2e

import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection
import com.luxoft.blockchainlab.corda.hyperledger.indy.PythonRefAgentConnection
import com.luxoft.blockchainlab.corda.hyperledger.indy.service.IndyPartyConnectionMock
import com.luxoft.web.clients.HospitalClient
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import rx.Single
import java.util.concurrent.TimeUnit

@RunWith(SpringRunner::class)
@ActiveProfiles(profiles = ["treatmentcenter"])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Ignore(
"""Use only for development/debugging
Preconditions: 
 - Set profile to one of {treatmentcenter, manufacture}
 - Run start.sh
 - Stop the service corresponding to the profile""")
class HospitalE2E : E2ETest()

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
//Precondition: Run start.sh
class RemoteE2E : E2ETest()

@Ignore("Do not use directly; Needs external setup")
@TestPropertySource(
    properties = [
        "hospitalEndpoint=http://localhost:8081"
    ]
)
@ImportAutoConfiguration(classes = [E2ETest.TestConfig::class])
abstract class E2ETest {
    @TestConfiguration
    class TestConfig {
        @Bean
        fun agentConnection(): AgentConnection = PythonRefAgentConnection().apply {
            connect(
                url = "ws://localhost:8094/ws",
                login = "agentUser",
                password = "password"
            ).toBlocking().value()
        }

        @Bean
        fun ssiUser() = IndyPartyConnectionMock.ssiTestUser()
    }

    @Autowired
    lateinit var hospitalClient: HospitalClient

    @Test
    fun mainFlow() {
        hospitalClient.authPatientFlow()
    }
}

inline fun waitThenAssert(
    tryCount: Int,
    retryDelaySec: Long = 1,
    noinline assertion: () -> Boolean
) {
    var i = 0
    while (i++ < tryCount) {
        if (!assertion())
            TimeUnit.SECONDS.sleep(retryDelaySec)
        else
            return
    }
    throw AssertionError("Waited $tryCount seconds")
}

fun <T> Single<T>.getValue() = this.timeout(60, TimeUnit.SECONDS).toBlocking().value()
