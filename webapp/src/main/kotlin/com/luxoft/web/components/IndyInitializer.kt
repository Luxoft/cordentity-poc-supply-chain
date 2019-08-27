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
import com.luxoft.poc.supplychain.data.schema.CertificateIndySchema
import com.luxoft.poc.supplychain.data.schema.PackageIndySchema
import net.corda.core.utilities.loggerFor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.Duration
import javax.annotation.PostConstruct

val timeout = Duration.ofSeconds(180L)

@Component
@Profile("treatmentcenter & corda")
class CordaIndyInitializerTreatmentCenter(val init: IdentityInitService) {
    private final val logger = loggerFor<CordaIndyInitializerTreatmentCenter>()

    @Autowired
    lateinit var rpc: RPCComponent

    @PostConstruct
    fun init() {
        val treatment = rpc.services

        if (treatment.vaultQuery(IndySchema::class.java).states.size < 2) {
            init.issueIndyMeta(PackageIndySchema, timeout)
            init.issueIndyMeta(CertificateIndySchema, timeout)

            logger.info("Treatment center indy stuff initialized")
        }

        logger.info("Initialization passed")
    }
}

@Component
@Profile("manufacture & corda")
class CordaIndyInitializerManufacture(val init: IdentityInitService) {
    private final val logger = loggerFor<CordaIndyInitializerManufacture>()

    @Autowired
    lateinit var rpc: RPCComponent

    @PostConstruct
    fun init() {
        val manufacture = rpc.services

        if (manufacture.vaultQuery(IndySchema::class.java).states.isEmpty()) {
            init.issueIndyMeta(CertificateIndySchema, timeout)

            logger.info("Manufacture indy stuff initialized")
        }

        logger.info("Initialization passed")
    }
}

@Component
@Profile("treatmentcenter & mock")
class MockIndyInitializerTreatmentCenter(val init: IdentityInitService) {
    private final val logger = loggerFor<MockIndyInitializerTreatmentCenter>()

    @PostConstruct
    fun init() {
        init.issueIndyMeta(PackageIndySchema, timeout)
        init.issueIndyMeta(CertificateIndySchema, timeout)

        logger.info("Treatment center indy stuff initialized")
        logger.info("Initialization passed")
    }
}

@Component
@Profile("manufacture & mock")
class IndyInitializerManufacture(val init: IdentityInitService) {
    private final val logger = loggerFor<IndyInitializerManufacture>()

    @PostConstruct
    fun init() {
        init.issueIndyMeta(CertificateIndySchema, timeout)

        logger.info("Manufacture indy stuff initialized")
        logger.info("Initialization passed")
    }
}
