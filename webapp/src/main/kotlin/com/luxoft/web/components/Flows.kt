package com.luxoft.web.components

import com.luxoft.blockchainlab.corda.hyperledger.indy.PythonRefAgentConnection
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.indyUser
import com.luxoft.blockchainlab.corda.hyperledger.indy.handle
import com.luxoft.blockchainlab.corda.hyperledger.indy.service.awaitFiber
import com.luxoft.blockchainlab.corda.hyperledger.indy.service.connectionService
import com.luxoft.blockchainlab.hyperledger.indy.IndyUser
import com.luxoft.blockchainlab.hyperledger.indy.SsiUser
import com.luxoft.blockchainlab.hyperledger.indy.helpers.*
import com.luxoft.blockchainlab.hyperledger.indy.ledger.IndyPoolLedgerUser
import com.luxoft.blockchainlab.hyperledger.indy.models.DidConfig
import com.luxoft.blockchainlab.hyperledger.indy.wallet.IndySDKWalletUser
import com.luxoft.blockchainlab.hyperledger.indy.wallet.getOwnIdentities
import com.luxoft.poc.supplychain.data.AcceptanceResult
import com.luxoft.poc.supplychain.data.PackageInfo
import com.luxoft.poc.supplychain.data.state.Package
import com.luxoft.poc.supplychain.flow.DeliverShipment
import com.luxoft.poc.supplychain.flow.GetInviteFlow
import com.luxoft.poc.supplychain.flow.PackageWithdrawal
import com.luxoft.poc.supplychain.flow.ReceiveShipment
import com.luxoft.poc.supplychain.flow.medicine.AskNewPackage
import com.luxoft.poc.supplychain.flow.medicine.GetPackageHistory
import com.luxoft.poc.supplychain.service.clientResolverService
import mu.KotlinLogging
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.startFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.io.File
import java.time.Duration
import java.util.*
import java.util.concurrent.CompletableFuture

interface CommonFlows {
    fun getNodeName(): String
    fun getPackageRequests(): List<PackageInfo>
    fun getPackageHistory(serial: String): String
}

interface TCFlows : CommonFlows {
    fun receiveShipment(result: AcceptanceResult)
    fun getInvite(uuid: UUID): String
    fun askNewPackage(uuid: UUID, string: String)
    fun packageWithdrawal(serial: String, clientId: UUID)
}

interface MFFlows : CommonFlows {
    fun deliverShipment(serial: String, name: CordaX500Name)
}

object Storage {
    val userUuid2Did = mutableMapOf<UUID, CompletableFuture<String>>()
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
class TCFlowsMock(val agentService: AgentService, val indyService: IndyService) : TCFlows {
    private val logger = KotlinLogging.logger {  }

    override fun getNodeName(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPackageRequests(): List<PackageInfo> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPackageHistory(serial: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun receiveShipment(result: AcceptanceResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getInvite(uuid: UUID): String {
        val invite = agentService.connection!!.generateInvite().awaitFiber()
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

    override fun askNewPackage(uuid: UUID, string: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun packageWithdrawal(serial: String, clientId: UUID) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

@Service
@Profile("mock")
class MFFlowsMock : MFFlows {
    override fun getNodeName() = "Manufacturer"

    override fun getPackageRequests(): List<PackageInfo> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPackageHistory(serial: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deliverShipment(serial: String, name: CordaX500Name) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

@Service
@Profile("corda")
class MFFlowsCorda(rpc: RPCComponent) : MFFlows {
    private final val services = rpc.services

    override fun getNodeName(): String {
        return services.nodeInfo().legalIdentities.first().name.organisation
    }

    override fun getPackageRequests(): List<PackageInfo> {
        return services.vaultQueryBy<Package>().states.map { it.state.data.info }
    }

    override fun getPackageHistory(serial: String): String {
        return services.startFlow(GetPackageHistory::Requester, serial).returnValue.get()
    }

    override fun deliverShipment(serial: String, name: CordaX500Name) {
        services.startFlowDynamic(DeliverShipment.Sender::class.java, serial, name).returnValue.get()
    }
}

@Service
@Profile("corda")
class TCFlowsCorda(rpc: RPCComponent) : TCFlows {
    private final val services = rpc.services

    override fun getNodeName(): String {
        return services.nodeInfo().legalIdentities.first().name.organisation
    }

    override fun getPackageRequests(): List<PackageInfo> {
        return services.vaultQueryBy<Package>().states.map { it.state.data.info }
    }

    override fun getPackageHistory(serial: String): String {
        return services.startFlow(GetPackageHistory::Requester, serial).returnValue.get()
    }

    override fun receiveShipment(result: AcceptanceResult) {
        services.startFlowDynamic(ReceiveShipment.Receiver::class.java, result)
    }

    override fun getInvite(uuid: UUID): String {
        return services.startFlow(GetInviteFlow::Treatment, uuid).returnValue.getOrThrow(Duration.ofSeconds(15))
    }

    override fun askNewPackage(uuid: UUID, did: String) {
        services.startFlow(AskNewPackage::Treatment, uuid, did)
    }

    override fun packageWithdrawal(serial: String, clientId: UUID) {
        services.startFlow(PackageWithdrawal::Owner, serial, clientId)
    }
}
