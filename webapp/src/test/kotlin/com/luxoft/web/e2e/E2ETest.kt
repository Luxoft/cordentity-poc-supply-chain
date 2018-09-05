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

import com.luxoft.poc.supplychain.data.PackageState
import com.luxoft.web.clients.ManufactureClient
import com.luxoft.web.clients.SovrinAgentClient
import com.luxoft.web.clients.TreatmentCenterClient
import com.luxoft.web.data.Package
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import javax.annotation.PostConstruct

@RunWith(SpringRunner::class)
@ActiveProfiles(profiles = ["manufacture", "treatmentcenter", "sovrinagent"])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class E2ETest {
    @LocalServerPort
    var localPort: Int = 0

    @Autowired
    lateinit var restTemplateBuilder: RestTemplateBuilder

    lateinit var manufactureClient: ManufactureClient
    lateinit var sovrinAgentClient: SovrinAgentClient
    lateinit var treatmentCenterClient: TreatmentCenterClient

    @PostConstruct
    fun initialize() {
        val customTemplate = restTemplateBuilder.rootUri("http://localhost:" + localPort).build()
        val restTemplate = TestRestTemplate(customTemplate)

        manufactureClient = ManufactureClient(restTemplate)
        sovrinAgentClient = SovrinAgentClient(restTemplate)
        treatmentCenterClient = TreatmentCenterClient(restTemplate)
    }

    @Test
    fun mainFlow() {
        `sovrin agent can issue new package`()
//        `manufacturer can process issued package`()
//        `sovrin agent can collect package`()
//        `treatment center can observe packages`()
    }

    fun `sovrin agent can issue new package`() {
//        val packagesBefore = sovrinAgentClient.getPackages()
//        val countBefore = packagesBefore.filter { packageHasStatus(it, PackageStatus.ISSUED) }.size

        sovrinAgentClient.initFlow("Treatment London GB")

//        val packagesAfter = sovrinAgentClient.getPackages()
//        val countAfter = packagesAfter.filter { packageHasStatus(it, PackageStatus.ISSUED) }.size
//
//        assert(countBefore < countAfter)
    }

    fun `sovrin agent can collect package`() {
        val packagesBefore = sovrinAgentClient.getPackages()
        assert(packagesBefore.isNotEmpty())

        val readyToCollectCountBefore = packagesBefore.filter { packageHasStatus(it, PackageState.QP_PASSED) }.size
        assert(readyToCollectCountBefore > 0)

        val readyToCollectPackage = packagesBefore.find { packageHasStatus(it, PackageState.QP_PASSED) }!!
        assertPackageValid(readyToCollectPackage)

        sovrinAgentClient.collectPackage(readyToCollectPackage.serial, "Treatment London GB")

        val packagesAfter = sovrinAgentClient.getPackages()
        assert(packagesAfter.isNotEmpty())

        val readyToCollectCountAfter = packagesAfter.filter { packageHasStatus(it, PackageState.QP_PASSED) }.size
        assert(readyToCollectCountBefore > readyToCollectCountAfter)
    }

    fun `manufacturer can process issued package`() {
        val packagesBefore = manufactureClient.getPackages()
        assert(packagesBefore.isNotEmpty())

        val issuedPackagesCountBefore = packagesBefore.filter { packageHasStatus(it, PackageState.ISSUED) }.size
        assert(issuedPackagesCountBefore > 0)

        val packageToProcess = packagesBefore.find { packageHasStatus(it, PackageState.ISSUED) }!!
        assertPackageValid(packageToProcess)

        manufactureClient.processPackage(packageToProcess.serial)

        val packagesAfter = manufactureClient.getPackages()
        assert(packagesAfter.isNotEmpty())

        val issuedPackagesCountAfter = packagesAfter.filter { packageHasStatus(it, PackageState.ISSUED) }.size
        assert(issuedPackagesCountBefore < issuedPackagesCountAfter)
    }

    fun `treatment center can observe packages`() {
        val packages = treatmentCenterClient.getPackages()
        assert(packages.isNotEmpty())
    }

    private fun packageHasStatus(pack: Package, status: PackageState): Boolean {
        return pack.status == status.ordinal
    }

    private fun assertPackageValid(pack: Package) {
        assert(pack.serial.isNotEmpty())
        assert(pack.status in 0..5)
        assert(pack.manufacturer.isNotEmpty())
        assert(pack.patientDid.isNotEmpty())
        assert(pack.patientDiagnosis.isNotEmpty())
        assert(pack.medicineName.isNotEmpty())
        assert(pack.medicineDescription.isNotEmpty())
        assert(pack.treatmentCenterName.isNotEmpty())
        assert(pack.treatmentCenterAddress.isNotEmpty())

        when (pack.status) {
            1 -> {
                assertWaypointPresence(pack.issuedAt, pack.issuedBy)
            }
            2 -> {
                assertWaypointPresence(pack.issuedAt, pack.issuedBy)
                assertWaypointPresence(pack.processedAt, pack.processedBy)
            }
            3 -> {
                assertWaypointPresence(pack.issuedAt, pack.issuedBy)
                assertWaypointPresence(pack.processedAt, pack.processedBy)
                assertWaypointPresence(pack.deliveredAt, pack.deliveredBy)
            }
            4 -> {
                assertWaypointPresence(pack.issuedAt, pack.issuedBy)
                assertWaypointPresence(pack.processedAt, pack.processedBy)
                assertWaypointPresence(pack.deliveredAt, pack.deliveredBy)
                assert(pack.qp)
            }
            5 -> {
                assertWaypointPresence(pack.issuedAt, pack.issuedBy)
                assertWaypointPresence(pack.processedAt, pack.processedBy)
                assertWaypointPresence(pack.deliveredAt, pack.deliveredBy)
                assert(pack.qp)
                assertWaypointPresence(pack.collectedAt, pack.collectedBy)
            }
        }
    }

    private fun assertWaypointPresence(at: Long, by: String) {
        assert(at > 0)
        assert(by.isNotEmpty())
    }
}
