package com.luxoft.lumedic.ssi.corda

import com.luxoft.lumedic.ssi.corda.flow.AuthPatient
import com.luxoft.lumedic.ssi.corda.service.EpicCommunicationService
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SsiFlowTests : CordaTestBase() {

    @Test
    fun mainFlow() {
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

}