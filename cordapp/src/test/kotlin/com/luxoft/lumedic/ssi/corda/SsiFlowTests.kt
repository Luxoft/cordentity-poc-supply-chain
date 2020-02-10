package com.luxoft.lumedic.ssi.corda

import com.luxoft.lumedic.ssi.corda.flow.AuthPatient
import com.luxoft.lumedic.ssi.corda.flow.DemoReset
import com.luxoft.lumedic.ssi.corda.service.EpicCommunicationService
import net.corda.core.contracts.ContractState
import net.corda.core.node.services.queryBy
import org.hyperledger.indy.sdk.LibIndy
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import java.util.concurrent.TimeUnit
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SsiFlowTests : CordaTestBase() {

    @get:Rule
    var globalTimeout: Timeout = Timeout(3, TimeUnit.MINUTES)

    //Test will be executed as part of resetFlow
    //@Test
    fun mainFlow() {
        //Test will run only if libindy installed
        assumeTrue(LibIndy.isInitialized())

        val authResponse = notary.runFlow(AuthPatient.Hospital("MRzYgbx16JDgukDLa2tuyk")).get()
        val proofInfo = EpicCommunicationService.submitInsurancePostSent.poll(1, TimeUnit.MINUTES)
        assertNotNull(proofInfo)
        proofInfo?.also { credentialProof ->
            assertTrue(credentialProof.getAttributeValue("Group_number")!!.raw.isNotBlank())
            assertTrue(credentialProof.getAttributeValue("Payor")!!.raw.isNotBlank())
            assertTrue(credentialProof.getAttributeValue("Insurance/member_ID")!!.raw.isNotBlank())
            assertTrue(credentialProof.getAttributeValue("Subscriber_date_of_birth_ms")!!.raw.toLong() > 0)
            assertTrue(credentialProof.getAttributeValue("Subscriber_name")!!.raw.isNotBlank())
        }
    }

    @Test
    fun resetFlow() {
        //Test will run only if libindy installed
        assumeTrue(LibIndy.isInitialized())

        fun getAllStates() = notary.services.vaultService.queryBy<ContractState>().states
        val beforeMainStates = getAllStates()
        mainFlow()

        val afterMainStates = getAllStates()
        assertTrue { beforeMainStates.count() < afterMainStates.count() }

        val txId = notary.runFlow(DemoReset.Hospital()).get()
        val afterResetStates = getAllStates()
        assertTrue { afterResetStates.isEmpty() }
    }

}