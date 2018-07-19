package com.luxoft.web.controllers

import com.luxoft.blockchainlab.corda.hyperledger.indy.data.state.IndyClaim
import com.luxoft.poc.supplychain.data.BusinessEntity
import com.luxoft.poc.supplychain.data.ChainOfAuthority
import com.luxoft.poc.supplychain.data.state.Package
import com.luxoft.poc.supplychain.flow.PackageWithdrawal
import com.luxoft.poc.supplychain.flow.medicine.AskNewPackage
import com.luxoft.poc.supplychain.issuerCert
import com.luxoft.poc.supplychain.treatmentCert
import com.luxoft.web.components.RPCComponent
import com.luxoft.web.data.AskForPackageRequest
import com.luxoft.web.data.FAILURE
import com.luxoft.web.data.PushToken
import com.luxoft.web.data.Serial
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.loggerFor
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("api/sa")
@CrossOrigin
@Profile("sovrinagent")
class SovrinAgentController(rpc: RPCComponent) {
    private final val services = rpc.services
    private final val logger = loggerFor<SovrinAgentController>()

    @GetMapping("whoami")
    fun getWhoAmI(): Any {
        return services.nodeInfo().legalIdentities.first().name.organisation
    }


    @PostMapping("request/create")
    fun createPackageRequest(@RequestBody tc: AskForPackageRequest): Any? {

        return try {
            val chainOfAuthority = ChainOfAuthority()
                    .add(BusinessEntity.Treatment, treatmentCert)
                    .add(BusinessEntity.Manufacturer, issuerCert)
                    .add(BusinessEntity.Insuranse, treatmentCert)
                    .add(BusinessEntity.Goverment, issuerCert)

            val flowHandle = services.startFlowDynamic(AskNewPackage.Patient::class.java, chainOfAuthority)
            val result = flowHandle.returnValue.get()
            return result

        } catch (e: Exception) {
            logger.error("", e)
            FAILURE.plus("error" to e.message)
        }
    }


    @PostMapping("package/withdraw")
    fun receivePackage(@RequestBody request: Serial): Any? {

        return try {

            val flowHandle = services.startFlowDynamic(PackageWithdrawal.Owner::class.java, request.serial)
            flowHandle.returnValue.get()
            null

        } catch (e: Exception) {
            logger.error("", e)
            FAILURE.plus("error" to e.message)
        }
    }

    @GetMapping("package/list")
    fun getPackageRequests(): Any {
        return try {
            services.vaultQueryBy<Package>().states.map { it.state.data.info }

        } catch (e: Exception) {
            logger.error("", e)
            FAILURE.plus("error" to e.message)
        }
    }

    @GetMapping("claim/list")
    fun getClaims(): Any {
        return try {
            services.vaultQueryBy<IndyClaim>().states.map { it.state.data.claim.json }

        } catch (e: Exception) {
            logger.error("", e)
            FAILURE.plus("error" to e.message)
        }
    }
}
