package com.luxoft.supplychain.sovrinagentapp.data

import java.time.Instant

data class VerificationEvent(
    val verificationInstant: Instant,
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
    VerifierInfo(did,
        name = "Cherry Hill Medical Center",
        address = "511 16th Ave, Seattle, WA 98122",
        contactPhone = "(206) 320-2000")

val dummyVerificationEvent = VerificationEvent(Instant.now(), emptySet(), verifierInfoFromDid("???"))