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
import com.luxoft.poc.supplychain.IdentityInitService
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
@Profile("treatmentcenter")
class IndyInitializerTreatmentCenter {
    private final val logger = loggerFor<IndyInitializerTreatmentCenter>()

    @Autowired
    lateinit var rpc: RPCComponent

    @PostConstruct
    fun init() {
        val treatment = rpc.services

        if (treatment.vaultQuery(IndySchema::class.java).states.size < 2) {
            val treatmentCenterIdentityService = IdentityInitService(treatment, timeout)
            treatmentCenterIdentityService.issueIndyMeta(PackageIndySchema)
            treatmentCenterIdentityService.issueIndyMeta(CertificateIndySchema)

            logger.info("Treatment center indy stuff initialized")
        }

        logger.info("Initialization passed")
    }
}

@Component
@Profile("manufacture")
class IndyInitializerManufacture {
    private final val logger = loggerFor<IndyInitializerManufacture>()

    @Autowired
    lateinit var rpc: RPCComponent

    @PostConstruct
    fun init() {
        val manufacture = rpc.services

        if (manufacture.vaultQuery(IndySchema::class.java).states.isEmpty()) {
            val initService = IdentityInitService(manufacture, timeout)
            initService.issueIndyMeta(CertificateIndySchema)

            logger.info("Manufacture indy stuff initialized")
        }

        logger.info("Initialization passed")
    }
}
