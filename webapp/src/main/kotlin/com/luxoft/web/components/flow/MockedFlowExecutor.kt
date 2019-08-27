package com.luxoft.web.components.flow

import com.luxoft.blockchainlab.corda.hyperledger.indy.IndyPartyConnection
import com.luxoft.blockchainlab.corda.hyperledger.indy.PythonRefAgentConnection
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.b2c.IssueCredentialFlowB2C
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.b2c.VerifyCredentialFlowB2C
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.indyUser
import com.luxoft.blockchainlab.corda.hyperledger.indy.handle
import com.luxoft.blockchainlab.hyperledger.indy.IndyUser
import com.luxoft.blockchainlab.hyperledger.indy.SsiUser
import com.luxoft.blockchainlab.hyperledger.indy.helpers.*
import com.luxoft.blockchainlab.hyperledger.indy.ledger.IndyPoolLedgerUser
import com.luxoft.blockchainlab.hyperledger.indy.models.*
import com.luxoft.blockchainlab.hyperledger.indy.utils.*
import com.luxoft.blockchainlab.hyperledger.indy.wallet.IndySDKWalletUser
import com.luxoft.blockchainlab.hyperledger.indy.wallet.getOwnIdentities
import com.luxoft.poc.supplychain.data.AcceptanceResult
import com.luxoft.poc.supplychain.data.PackageInfo
import com.luxoft.poc.supplychain.data.PackageState
import com.luxoft.poc.supplychain.data.schema.PackageIndySchema
import com.luxoft.poc.supplychain.flow.IndyResolver
import com.luxoft.poc.supplychain.flow.getManufacturer
import com.luxoft.poc.supplychain.flow.getRevocationRegistryLike
import mu.KotlinLogging
import net.corda.core.flows.FlowException
import net.corda.core.identity.CordaX500Name
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.io.File
import java.util.*
import java.util.concurrent.CompletableFuture

object Storage {
    val userUuid2Did = mutableMapOf<UUID, CompletableFuture<String>>()
    val packages = mutableMapOf<String, PackageInfo>()
}

@Service
@Profile("mock")
class AgentService {
    @Value("agent.endpoint:ws://localhost:8095/ws")
    var agentWsEndpoint: String? = null

    @Value("agent.login:treatmentCenter")
    var agentLogin: String? = null

    @Value("agent.password:secretPassword")
    var agentPassword: String? = null

    val connection by lazy {
        if (agentWsEndpoint != null) {
            agentLogin ?: throw RuntimeException("Agent websocket endpoint specified but agent user name is missing")
            agentPassword ?: throw RuntimeException("Agent websocket endpoint specified but agent password is missing")

            PythonRefAgentConnection().apply { connect(agentWsEndpoint!!, agentLogin!!, agentPassword!!).toBlocking().value() }
        } else
            null
    }
}

@Service
@Profile("mock")
class IndyService {
    private val logger = KotlinLogging.logger {}

    @Value("indy.wallet.name:TreatmentCenter")
    private var walletName: String? = ""

    @Value("indy.wallet.password:secretPassword")
    private var walletPassword: String? = ""

    @Value("indy.genesis.filepath:genesis/docker.txn")
    private val genesisFilePath: String? = ""

    private val poolName = PoolHelper.DEFAULT_POOL_NAME

    @Value("indy.did:XmLm4WJnNx5poPMqrcgg3q")
    private val did = ConfigHelper.getDid()

    @Value("indy.seed:0000000000000000TreatmentCenter1")
    private val seed = ConfigHelper.getSeed()

    val indyUser: SsiUser by lazy {
        val nodeName = "TreatmentCenter"

        walletName ?: throw RuntimeException("Wallet name should be specified in config")
        walletPassword ?: throw RuntimeException("Wallet password should be specified in config")

        val wallet = WalletHelper.openOrCreate(walletName!!, walletPassword!!)
        logger.debug { "Wallet created for $nodeName" }

        val tailsPath = "tails"
        val didConfig = DidConfig(did, seed, null, null)

        val walletUser = if (did != null && wallet.getOwnIdentities().map { it.did }.contains(did))
            IndySDKWalletUser(wallet, did, tailsPath).also {
                logger.debug { "Found user with did $did in wallet" }
            }
        else
            IndySDKWalletUser(wallet, didConfig, tailsPath).also {
                logger.debug { "Created new user with did $did in wallet" }
            }

        logger.debug { "IndyUser object created for $nodeName" }

        genesisFilePath ?: throw RuntimeException("Genesis file path should be specified in config")
        val genesisFile = File(genesisFilePath)
        if (!GenesisHelper.exists(genesisFile))
            throw RuntimeException("Genesis file doesn't exist")

        val pool = PoolHelper.openOrCreate(genesisFile, poolName)
        logger.debug { "Pool $poolName opened for $nodeName" }

        val ledgerUser = IndyPoolLedgerUser(pool, walletUser.getIdentityDetails().did) { walletUser.sign(it) }

        IndyUser.with(walletUser).with(ledgerUser).build()
    }
}

@Service
@Profile("mock")
class IndyFlows(val agentService: AgentService, val indyService: IndyService) {
    val issuer = object : Issuer {
        override val schemas = hashMapOf<SchemaId, Schema>()
        override val credentialDefinitions = hashMapOf<CredentialDefinitionId, Pair<CredentialDefinition, SchemaId>>()
        override val revocationRegistryDefinitions = hashMapOf<RevocationRegistryDefinitionId, Triple<RevocationRegistryDefinition, CredentialDefinitionId, SchemaId>>()

        override fun createSchemaFlow(name: String, version: String, attributes: List<String>) =
            indyService.indyUser.createSchemaAndStoreOnLedger(name, version, attributes).also {
                schemas[it.getSchemaIdObject()] = it
            }

        override fun createCredentialDefinitionFlow(schemaId: SchemaId, enableRevocation: Boolean) =
            indyService.indyUser.createCredentialDefinitionAndStoreOnLedger(schemaId, enableRevocation).also {
                credentialDefinitions[it.getCredentialDefinitionIdObject()] = Pair(it, schemaId)
            }

        override fun createRevocationRegistryFlow(credentialDefinitionId: CredentialDefinitionId, credentialCapacity: Int) =
            indyService.indyUser.createRevocationRegistryAndStoreOnLedger(credentialDefinitionId, credentialCapacity).also {
                revocationRegistryDefinitions[it.definition.getRevocationRegistryIdObject()!!] = Triple(it.definition, credentialDefinitionId, credentialDefinitions[credentialDefinitionId]!!.second)
            }

        override fun issueCredentialFlow(
            proverDid: String,
            credentialDefinitionId: CredentialDefinitionId,
            revocationRegistryDefinitionId: RevocationRegistryDefinitionId?,
            credentialProposalFiler: CredentialProposal.() -> Unit
        ): CredentialInfo {
            val proverConnection = agentService.connection!!.getIndyPartyConnection(proverDid).toBlocking().value()!!

            val offer = indyService.indyUser.createCredentialOffer(credentialDefinitionId)

            proverConnection.sendCredentialOffer(offer)
            val request = proverConnection.receiveCredentialRequest().toBlocking().value()

            val credential = indyService.indyUser.issueCredentialAndUpdateLedger(request, offer, revocationRegistryDefinitionId, credentialProposalFiler)
            proverConnection.sendCredential(credential)

            return credential
        }
    }

    val prover = object : Prover {
        override fun issuerCredentialFlow(issuerDid: String): CredentialInfo {
            val issuerConnection = agentService.connection!!.getIndyPartyConnection(issuerDid).toBlocking().value()!!

            val offer = issuerConnection.receiveCredentialOffer().toBlocking().value()
            val request = indyService.indyUser.createCredentialRequest(indyService.indyUser.walletUser.getIdentityDetails().did, offer)
            issuerConnection.sendCredentialRequest(request)

            val credential = issuerConnection.receiveCredential().toBlocking().value()
            indyService.indyUser.checkLedgerAndReceiveCredential(credential, request, offer)

            return credential
        }

        override fun verifyCredentialFlow(verifierDid: String) {
            val verifierConnection = agentService.connection!!.getIndyPartyConnection(verifierDid).toBlocking().value()!!

            val proofRequest = verifierConnection.receiveProofRequest().toBlocking().value()

            val proof = indyService.indyUser.createProofFromLedgerData(proofRequest)
            verifierConnection.sendProof(proof)
        }
    }

    val verifier = object : Verifier {
        override fun verifyCredentialFlow(proverDid: String, proofRequest: ProofRequest): Boolean {
            val proverConnection = agentService.connection!!.getIndyPartyConnection(proverDid).toBlocking().value()!!

            proverConnection.sendProofRequest(proofRequest)
            val proof = proverConnection.receiveProof().toBlocking().value()

            return indyService.indyUser.verifyProofWithLedgerData(proofRequest, proof)
        }
    }
}

interface Verifier {
    fun verifyCredentialFlow(proverDid: String, proofRequest: ProofRequest): Boolean
}

interface Prover {
    fun issuerCredentialFlow(issuerDid: String): CredentialInfo
    fun verifyCredentialFlow(verifierDid: String)
}

interface Issuer {
    val schemas: MutableMap<SchemaId, Schema>
    val credentialDefinitions: MutableMap<CredentialDefinitionId, Pair<CredentialDefinition, SchemaId>>
    val revocationRegistryDefinitions: MutableMap<RevocationRegistryDefinitionId, Triple<RevocationRegistryDefinition, CredentialDefinitionId, SchemaId>>

    fun getSchemaLike(name: String) = schemas.entries.find { it.key.name == name }?.value
    fun getCredentialDefinitionLike(schemaName: String) = credentialDefinitions.values.find { it.second.name == schemaName }?.first
    fun getRevocationRegistryLike(schemaName: String) = revocationRegistryDefinitions.values.find { it.third.name == schemaName }?.first

    fun createSchemaFlow(name: String, version: String, attributes: List<String>): Schema
    fun createCredentialDefinitionFlow(schemaId: SchemaId, enableRevocation: Boolean): CredentialDefinition
    fun createRevocationRegistryFlow(
        credentialDefinitionId: CredentialDefinitionId,
        credentialCapacity: Int
    ): RevocationRegistryInfo
    fun issueCredentialFlow(
        proverDid: String,
        credentialDefinitionId: CredentialDefinitionId,
        revocationRegistryDefinitionId: RevocationRegistryDefinitionId?,
        credentialProposalFiler: CredentialProposal.() -> Unit
    ): CredentialInfo
}

@Service
@Profile("treatmentcenter & mock")
class TCFlowsMock(val agentService: AgentService, val indyService: IndyService, val indyFlowExecutor: IndyFlows) : TCFlows {
    private val logger = KotlinLogging.logger {  }

    override fun getNodeName() = "TreatmentCenter"

    override fun getPackageRequests(): List<PackageInfo> {
        return Storage.packages.values.toList()
    }

    override fun getPackageHistory(serial: String): String {
        return ""
    }

    override fun receiveShipment(result: AcceptanceResult) {
        val newPackage = Storage.packages[result.serial]!!.copy(
            state = PackageState.DELIVERED,
            deliveredAt = System.currentTimeMillis(),
            deliveredTo = CordaX500Name("Treatment Center", "London", "UK")
        )

        Storage.packages[result.serial] = newPackage
    }

    override fun getInvite(uuid: UUID): String {
        val invite = agentService.connection!!.generateInvite().toBlocking().value()
        Storage.userUuid2Did[uuid] = CompletableFuture()
        CompletableFuture.runAsync {
            agentService.connection!!.waitForInvitedParty(invite, 300000)
                .handle { message, ex ->
                    if (ex != null) {
                        logger.error("Failed to wait for invited party", ex)
                        return@handle
                    }
                    Storage.userUuid2Did[uuid]!!.complete(message!!.partyDID())
                    message.handleTailsRequestsWith {
                        TailsHelper.DefaultReader(indyService.indyUser.walletUser.getTailsPath()).read(it)
                    }
                }
        }.exceptionally { logger.error("Error in invite future", it); null; }

        return invite
    }

    override fun askNewPackage(uuid: UUID, issuerDid: String) {
        val clientDid = Storage.userUuid2Did[uuid]!!.get()
        val serial = UUID.randomUUID().toString()

        checkPermissions(clientDid, issuerDid)
        issueReceipt(clientDid, serial)

        Storage.packages[serial] = PackageInfo(
            serial = serial,
            state = PackageState.NEW,
            patientDid = clientDid,
            patientDiagnosis = "leukemia",
            medicineName = "Santorium",
            medicineDescription = "package-required",
            requestedAt = System.currentTimeMillis(),
            requestedBy = CordaX500Name("Treatment Center", "London", "UK"),
            processedBy = CordaX500Name("Manufacture", "London", "UK")
        )
    }

    private fun checkPermissions(clientDid: String, issuerDid: String) {
        val proofRequest = proofRequest("user_proof_req", "1.0") {
            reveal("name")
            reveal("sex")
            reveal("medical id") { FilterProperty.IssuerDid shouldBe issuerDid }
            reveal("medical condition") { FilterProperty.IssuerDid shouldBe issuerDid }
            proveGreaterThan("age", 18)
        }

        if (!indyFlowExecutor.verifier.verifyCredentialFlow(clientDid, proofRequest))
            throw throw FlowException("Permission verification failed")
    }

    private fun issueReceipt(clientDid: String, serial: String) {
        val revocationRegistryDefinition = indyFlowExecutor.issuer.getRevocationRegistryLike(PackageIndySchema.schemaName)!!

        indyFlowExecutor.issuer.issueCredentialFlow(
            clientDid,
            revocationRegistryDefinition.getCredentialDefinitionIdObject(),
            revocationRegistryDefinition.getRevocationRegistryIdObject()
        ) {
            attributes["serial"] = CredentialValue(serial)
            attributes["time"] = CredentialValue(System.currentTimeMillis().toString())
        }
    }

    override fun packageWithdrawal(serial: String, clientId: UUID) {
        val issuerDid = indyService.indyUser.walletUser.getIdentityDetails().did
        val proverDid = Storage.userUuid2Did[clientId]!!.get()

        verifyReceipt(serial, issuerDid, proverDid)
        val newPack = Storage.packages[serial]!!
            .copy(state = PackageState.COLLECTED, collectedAt = System.currentTimeMillis())

        Storage.packages[serial] = newPack
    }

    private fun verifyReceipt(serial: String, issuerDid: String, clientDid: String) {
        val serialProofRequest = proofRequest("proof_req", "1.0") {
            reveal("serial") {
                "serial" shouldBe serial
                FilterProperty.IssuerDid shouldBe issuerDid
            }
            proveNonRevocation(Interval.allTime())
        }
        indyFlowExecutor.verifier.verifyCredentialFlow(clientDid, serialProofRequest)
    }
}

@Service
@Profile("manufacture & mock")
class MFFlowsMock : MFFlows {
    override fun getNodeName() = "Manufacturer"

    override fun getPackageRequests(): List<PackageInfo> {
        return Storage.packages.values.toList()
    }

    override fun getPackageHistory(serial: String): String {
        return ""
    }

    override fun deliverShipment(serial: String, name: CordaX500Name) {
        val newPackage = Storage.packages[serial]!!.copy(
            state = PackageState.PROCESSED,
            processedAt = System.currentTimeMillis(),
            processedBy = CordaX500Name("Manufacture", "London", "UK")
        )

        Storage.packages[serial] = newPackage
    }
}
