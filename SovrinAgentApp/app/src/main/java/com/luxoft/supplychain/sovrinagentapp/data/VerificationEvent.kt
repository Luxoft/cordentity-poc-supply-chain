package com.luxoft.supplychain.sovrinagentapp.data

import com.luxoft.blockchainlab.hyperledger.indy.models.ProofInfo
import com.luxoft.blockchainlab.hyperledger.indy.models.ProofRequest
import java.time.Instant

data class VerificationEvent(
    val verificationInstant: Instant,
    val proof: ProofInfo,
    val proofRequest: ProofRequest,
    val requestedAttributeNames: Set<String>,
    val verifier: VerifierInfo
)

data class VerifierInfo(
    val did: String,
    val name: String,
    val address: String,
    val contactPhone: String
)

fun verifierInfoFromDid(did: String) =
    VerifierInfo("",
        name = "",
        address = "",
        contactPhone = "")