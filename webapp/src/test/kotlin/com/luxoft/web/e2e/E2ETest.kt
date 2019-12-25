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
import com.luxoft.poc.supplychain.data.PackageInfo
import com.luxoft.poc.supplychain.data.PackageState
import com.luxoft.web.clients.ManufactureClient
import com.luxoft.web.clients.TreatmentCenterClient
import net.corda.core.identity.CordaX500Name
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
class TreatmentCenterE2E : E2ETest()

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Ignore("Precondition: Run start.sh")
class RemoteE2E : E2ETest()

@Ignore("Do not use directly; Needs external setup")
@TestPropertySource(
    properties = [
        "manufactureEndpoint=http://localhost:8081",
        "treatmentCenterEndpoint=http://localhost:8082"
    ]
)
@ImportAutoConfiguration
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

            val genesisFile = File("../cordapp/src/main/resources/genesis/indy_pool_dev.txn")
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
    lateinit var manufactureClient: ManufactureClient

    @Autowired
    lateinit var treatmentCenterClient: TreatmentCenterClient

    @Test
    fun mainFlow() {
        `treatment center can issue new package`()
        `manufacturer can process issued package`()
        `treatment center can receive package`()
        `treatment center can give package`()
        `treatment center can observe packages`()
        `manufacturer can provide history for package`()
    }

    val syncUpRetry = 15

    fun `treatment center can issue new package`() {
        val packagesBefore = treatmentCenterClient.getPackages()
        val packagesCountBefore = packagesBefore.filter { packageHasStatus(it, PackageState.ISSUED) }.size

        //TODO: Take credentials required for initFlow
        val invite = treatmentCenterClient.getInvite()
        treatmentCenterClient.initFlow("TC SEEHOF Zurich CH", invite)


        waitThenAssert(syncUpRetry) {
            val packagesAfter = treatmentCenterClient.getPackages()
            val packagesCountAfter = packagesAfter.filter { packageHasStatus(it, PackageState.ISSUED) }.size

            packagesCountBefore < packagesCountAfter
        }
    }

    fun `treatment center can receive package`() {
        waitThenAssert(syncUpRetry) {
            val packagesAfter = treatmentCenterClient.getPackages()
            packagesAfter.filter { packageHasStatus(it, PackageState.PROCESSED) }.firstOrNull() != null
        }

        val packagesBefore = treatmentCenterClient.getPackages()
        assert(packagesBefore.isNotEmpty())

        val packagesCountBefore = packagesBefore.filter { packageHasStatus(it, PackageState.DELIVERED) }.size

        val readyToReceivePackage = packagesBefore.findLast { packageHasStatus(it, PackageState.PROCESSED) }!!
        assertPackageValid(readyToReceivePackage)

        treatmentCenterClient.receivePackage(readyToReceivePackage.serial)

        waitThenAssert(syncUpRetry) {
            val packagesAfter = treatmentCenterClient.getPackages()
            val packagesCountAfter = packagesAfter.filter { packageHasStatus(it, PackageState.DELIVERED) }.size

            packagesCountBefore < packagesCountAfter
        }
    }

    fun `treatment center can give package`() {
        val packagesBefore = treatmentCenterClient.getPackages()
        assert(packagesBefore.isNotEmpty())

        val packagesCountBefore = packagesBefore.filter { packageHasStatus(it, PackageState.COLLECTED) }.size

        val readyToGivePackage = packagesBefore.findLast { packageHasStatus(it, PackageState.DELIVERED) }!!
        assertPackageValid(readyToGivePackage)

        treatmentCenterClient.collectPackage(readyToGivePackage.serial, treatmentCenterClient.getInvite())

        waitThenAssert(syncUpRetry) {
            val packagesAfter = treatmentCenterClient.getPackages()
            val packagesCountAfter = packagesAfter.filter { packageHasStatus(it, PackageState.COLLECTED) }.size

            packagesCountBefore < packagesCountAfter
        }
    }

    fun `manufacturer can process issued package`() {
        val packagesBefore = manufactureClient.getPackages()
        assert(packagesBefore.isNotEmpty())

        val packagesCountBefore = packagesBefore.filter { packageHasStatus(it, PackageState.PROCESSED) }.size

        val packageToProcess = packagesBefore.findLast { packageHasStatus(it, PackageState.ISSUED) }!!
        assertPackageValid(packageToProcess)

        manufactureClient.processPackage(packageToProcess.serial)

        waitThenAssert(syncUpRetry) {
            val packagesAfter = manufactureClient.getPackages()
            val packagesCountAfter = packagesAfter.filter { packageHasStatus(it, PackageState.PROCESSED) }.size

            packagesCountBefore < packagesCountAfter
        }
    }

    fun `manufacturer can provide history for package`() {
        val packagesBefore = treatmentCenterClient.getPackages()
        assert(packagesBefore.isNotEmpty())

        val packageToProcess = packagesBefore.findLast { packageHasStatus(it, PackageState.COLLECTED) }!!
        assertPackageValid(packageToProcess)

        manufactureClient.packageHistory(packageToProcess.serial)
    }

    fun `treatment center can observe packages`() {
        val packages = treatmentCenterClient.getPackages()
        assert(packages.isNotEmpty())
    }

    private fun packageHasStatus(pack: PackageInfo, status: PackageState): Boolean {
        return pack.state == status
    }

    private fun assertPackageValid(pack: PackageInfo) {
        assert(pack.serial.isNotEmpty())
        assert(pack.state.name.isNotBlank())
        assert(pack.patientDid.isNotEmpty())
        assert(pack.patientDiagnosis?.isNotEmpty() ?: false)
        assert(pack.medicineName?.isNotEmpty() ?: false)

        when (pack.state) {
            PackageState.NEW -> {
                assertWaypointPresence(pack.requestedAt, pack.requestedBy)
            }
            PackageState.ISSUED -> {
                assertWaypointPresence(pack.requestedAt, pack.requestedBy)
                assertWaypointPresence(pack.issuedAt, pack.issuedBy)
            }
            PackageState.PROCESSED -> {
                assertWaypointPresence(pack.requestedAt, pack.requestedBy)
                assertWaypointPresence(pack.issuedAt, pack.issuedBy)
                assertWaypointPresence(pack.processedAt, pack.processedBy)
            }
            PackageState.DELIVERED -> {
                assertWaypointPresence(pack.requestedAt, pack.requestedBy)
                assertWaypointPresence(pack.issuedAt, pack.issuedBy)
                assertWaypointPresence(pack.processedAt, pack.processedBy)
                assertWaypointPresence(pack.deliveredAt, pack.deliveredTo)
//                assert(pack.qp ?: false)
            }
            PackageState.COLLECTED -> {
                assertWaypointPresence(pack.requestedAt, pack.requestedBy)
                assertWaypointPresence(pack.issuedAt, pack.issuedBy)
                assertWaypointPresence(pack.processedAt, pack.processedBy)
                assertWaypointPresence(pack.deliveredAt, pack.deliveredTo)
//                assert(pack.qp ?: false)
                assertWaypointPresence(pack.collectedAt, pack.deliveredTo)
            }
            else -> assert(false)
        }
    }

    private fun assertWaypointPresence(at: Long?, by: CordaX500Name?) {
        assert(at ?: 0 > 0)
        assert(by?.organisation?.isNotEmpty() ?: false)
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

}

fun <T> Single<T>.getValue() = this.timeout(15, TimeUnit.SECONDS).toBlocking().value()
