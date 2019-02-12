package com.luxoft.supplychain.sovrinagentapp.indy

import com.luxoft.blockchainlab.hyperledger.indy.*
import java.lang.IllegalStateException
import java.lang.RuntimeException
import java.util.*
import kotlin.math.absoluteValue

typealias Did = String

data class ProvisionConfig(
        val agent: IndyParty
)

data class IndyParty(val did: Did, val verkey: String, val endpoint: String)

interface Connection {
    fun getCounterParty(): IndyParty

    fun sendCredentialOffer(offer: CredentialOffer)
    fun receiveCredentialOffer(): CredentialOffer

    fun sendCredentialRequest(request: CredentialRequestInfo)
    fun receiveCredentialRequest(): CredentialRequestInfo

    fun sendCredential(credential: CredentialInfo)
    fun receiveCredential(): CredentialInfo

    fun sendProofRequest(request: ProofRequest)
    fun receiveProofRequest(): ProofRequest

    fun sendProof(proof: Proof)
    fun receiveProof(): Proof
}

interface IndyAgentService {
    fun provision(config: ProvisionConfig)
    fun establishConnection(with: Did): Connection
}

class DummyConnection(val partyIndyUser: IndyUser): Connection {
    var schema: Schema
    var credDef: CredentialDefinition

    init {
        val version = "${Random().nextInt().absoluteValue % 100}.${Random().nextInt().absoluteValue % 100}.${Random().nextInt().absoluteValue % 100}"
        val attributes = listOf("sex", "name", "height", "age")

        schema = partyIndyUser.createSchema("example", version, attributes)
        credDef = partyIndyUser.createCredentialDefinition(schema.getSchemaId(), false)
    }

    override fun getCounterParty(): IndyParty {
        return IndyParty(partyIndyUser.did, partyIndyUser.verkey, "localhost:123")
    }

    val errorText = "Dummy connection is only able to play client's role"

    override fun sendCredentialOffer(offer: CredentialOffer) {
        throw RuntimeException(errorText)
    }

    var credentialOffer: CredentialOffer? = null
    override fun receiveCredentialOffer(): CredentialOffer {
        credentialOffer = partyIndyUser.createCredentialOffer(credDef.getCredentialDefinitionId())

        return credentialOffer!!
    }

    var credentialRequest: CredentialRequestInfo? = null
    override fun sendCredentialRequest(request: CredentialRequestInfo) {
        credentialRequest = request
    }

    override fun receiveCredentialRequest(): CredentialRequestInfo {
        throw RuntimeException(errorText)
    }

    override fun sendCredential(credential: CredentialInfo) {
        throw RuntimeException(errorText)
    }

    var credential: CredentialInfo? = null
    override fun receiveCredential(): CredentialInfo {
        val credValues = """
            {
                "sex": {"raw": "male", "encoded": "5944657099558967239210949258394887428692050081607692519917050"},
                "name": {"raw": "Alex", "encoded": "1139481716457488690172217916278103335"},
                "height": {"raw": "175", "encoded": "175"},
                "age": {"raw": "28", "encoded": "28"}
            }
        """.trimIndent()

        credential = partyIndyUser.issueCredential(credentialRequest!!, credValues, credentialOffer!!)

        return credential!!
    }

    override fun sendProofRequest(request: ProofRequest) {
        throw RuntimeException(errorText)
    }

    var proofReq: ProofRequest? = null
    override fun receiveProofRequest(): ProofRequest {
        val field_name = CredentialFieldReference("name", schema.id, credDef.id)
        val field_sex = CredentialFieldReference("sex", schema.id, credDef.id)
        val field_age = CredentialFieldReference("age", schema.id, credDef.id)
        proofReq = IndyUser.createProofRequest(
                version = "0.1",
                name = "proof_req_0.1",
                attributes = listOf(field_name, field_sex),
                predicates = listOf(CredentialPredicate(field_age, 18)),
                nonRevoked = null
        )

        return proofReq!!
    }

    override fun sendProof(proof: Proof) {
    }

    override fun receiveProof(): Proof {
        throw RuntimeException(errorText)
    }
}

class DummyIndyAgentService(val me: IndyUser, private val counterParty: IndyUser): IndyAgentService {
    private var isProvisioned: Boolean = false

    override fun provision(config: ProvisionConfig) {
        isProvisioned = true
    }

    override fun establishConnection(with: Did): Connection {
        if (!isProvisioned)
            throw IllegalStateException("You should perform agent provision before")

        return DummyConnection(counterParty)
    }
}