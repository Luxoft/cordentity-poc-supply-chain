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
import com.luxoft.blockchainlab.hyperledger.indy.IndyUser
import com.luxoft.blockchainlab.hyperledger.indy.SsiUser
import com.luxoft.blockchainlab.hyperledger.indy.helpers.GenesisHelper
import com.luxoft.blockchainlab.hyperledger.indy.helpers.PoolHelper
import com.luxoft.blockchainlab.hyperledger.indy.helpers.WalletHelper
import com.luxoft.blockchainlab.hyperledger.indy.ledger.IndyPoolLedgerUser
import com.luxoft.blockchainlab.hyperledger.indy.wallet.IndySDKWalletUser
import com.luxoft.web.clients.HospitalClient
import org.apache.commons.io.FileUtils
import org.hyperledger.indy.sdk.pool.Pool
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
import java.io.File
import java.util.*
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
        fun ssiUser(): SsiUser {
            val pool: Pool
            val poolName: String = "test-pool-${Math.abs(Random().nextInt())}"
            val tmpTestWalletId = "tmpTestWallet${Math.abs(Random().nextInt())}"

            val genesisFile = File("../devops/profile/develop/genesis/indy_pool_lumedic.txn")
            if (!GenesisHelper.exists(genesisFile))
                throw RuntimeException("Genesis file ${genesisFile.absolutePath} doesn't exist")

            PoolHelper.createOrTrunc(genesisFile, poolName)
            pool = PoolHelper.openExisting(poolName)

            //creating user wallet with credentials required by logic
            File(this.javaClass.classLoader.getResource("testUserWallet.db").file)
                .copyTo(File("${FileUtils.getUserDirectory()}/.indy_client/wallet/$tmpTestWalletId/sqlite.db"))
                .apply {
                    Runtime.getRuntime().addShutdownHook(Thread { FileUtils.deleteQuietly(parentFile) })
                }

            val wallet = WalletHelper.openExisting(tmpTestWalletId, "password")

            val walletUser = IndySDKWalletUser(wallet)
            val ledgerUser = IndyPoolLedgerUser(pool, walletUser.getIdentityDetails().did) { walletUser.sign(it) }
            return IndyUser.with(walletUser).with(ledgerUser).build()
        }
    }

    @Autowired
    lateinit var hospitalClient: HospitalClient

    @Test
    fun mainFlow() {
        repeat(2) {
            hospitalClient.authPatientFlow()
            hospitalClient.demoReset()
        }
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
