package com.luxoft.web.controllers

import com.luxoft.poc.supplychain.data.state.Package
import com.luxoft.poc.supplychain.flow.DeliverShipment
import com.luxoft.poc.supplychain.flow.QPReleaseFlow
import com.luxoft.web.components.RPCComponent
import com.luxoft.web.data.FAILURE
import com.luxoft.web.data.QPReleaseResult
import com.luxoft.web.data.Serial
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.loggerFor
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("api/mf")
@CrossOrigin
@Profile("manufacture")
class ManufactureController(rpc: RPCComponent) {
    private final val services = rpc.services
    private final val logger = loggerFor<ManufactureController>()

    @GetMapping("whoami")
    fun getWhoAmI(): Any {
        return services.nodeInfo().legalIdentities.first().name.organisation
    }

    @GetMapping("package/list")
    fun getPackageRequests(): Any {

        return try {
            val criteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
            val a = services.vaultQueryBy<Package>().states.map { it.state.data.info }

            logger.info(a.size.toString())

            a
        } catch (e: Exception) {
            logger.error("", e)
            FAILURE.plus("error" to e.message)
        }
    }

    @PostMapping("request/process")
    fun processPackageRequest(@RequestBody serial: Serial): Any? {

        return try {

            val flowHandle = services.startFlowDynamic(DeliverShipment.Sender::class.java, serial.serial, CordaX500Name("TreatmentCenter", "London", "GB"))
            flowHandle.returnValue.get()
            null

        } catch (e: Exception) {
            logger.error("", e)
            FAILURE.plus("error" to e.message)
        }
    }


    @PostMapping("qp/finish")
    fun qpRelease(@RequestBody serial: Serial): Any {

        return try {

            val flowHandle = services.startFlowDynamic(QPReleaseFlow.QP::class.java, serial.serial)
            val status = flowHandle.returnValue.get()

            QPReleaseResult(serial.serial, status)

        } catch (e: Exception) {
            logger.error("", e)
            FAILURE.plus("error" to e.message)
        }
    }
}