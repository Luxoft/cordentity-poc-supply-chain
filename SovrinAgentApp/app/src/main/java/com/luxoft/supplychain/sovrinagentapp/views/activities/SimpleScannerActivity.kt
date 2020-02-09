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

package com.luxoft.supplychain.sovrinagentapp.views.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection
import com.luxoft.blockchainlab.hyperledger.indy.IndyUser
import com.luxoft.blockchainlab.hyperledger.indy.models.CredentialReference
import com.luxoft.blockchainlab.hyperledger.indy.models.ProofRequest
import com.luxoft.blockchainlab.hyperledger.indy.utils.FilterProperty
import com.luxoft.blockchainlab.hyperledger.indy.utils.SerializationUtils
import com.luxoft.blockchainlab.hyperledger.indy.utils.proofRequest
import com.luxoft.blockchainlab.hyperledger.indy.utils.reveal
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.application.*
import com.luxoft.supplychain.sovrinagentapp.data.communcations.SovrinAgentService
import com.luxoft.supplychain.sovrinagentapp.data.*
import com.luxoft.supplychain.sovrinagentapp.utils.Resource
import com.luxoft.supplychain.sovrinagentapp.utils.ResourceState
import com.luxoft.supplychain.sovrinagentapp.utils.updateCredentialsInRealm
import com.luxoft.supplychain.sovrinagentapp.viewmodel.IndyViewModel
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_scanner.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.viewModel
import retrofit.GsonConverterFactory
import retrofit.Retrofit
import retrofit.RxJavaCallAdapterFactory
import rx.Completable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger

class SimpleScannerActivity : AppCompatActivity() {

    private val appState: ApplicationState by inject()

    private val api: SovrinAgentService by inject()
//    private val indyUser: IndyUser by inject()
    private val agentConnection: AgentConnection by inject()
    private val requestCodeScan = 101

    private val correctInvite = Regex(".+\\/indy\\?c_i=.+")
    private val correctUtl = Regex("http:\\/\\/.+mf\\/package\\/history")
    private var collectedAt: Long? = 0

    private val sharedPreferencesName = "REQUESTED_DATA_SP"
    private val sharedPreferencesKey = "REQUESTED_DATA_KEY"

    private val vm: IndyViewModel by viewModel()

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_scanner)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_back)
        }

        IntentIntegrator(this).setPrompt("SCAN").setOrientationLocked(false).initiateScan()
    }

    private fun updateResponseCredentialsGot(resource: Resource<String>?) {
        resource?.let {
            when (it.state) {
                ResourceState.LOADING -> {
                    setStatusName(R.string.state_get_proofs)
                    publishProgress(R.string.progress_accept_invite)
                }
                ResourceState.SUCCESS -> if (it.data.equals("completed")) notifyAndFinish(R.string.progress_state_get_proofs_finished) else it.data?.let { it1 -> publishProgress(it1) }
                ResourceState.ERROR -> notifyAndFinish("Get Claims Error: ${it.message}")
            }
        }
    }

    private fun updateResponseProfSent(resource: Resource<String>?) {
        resource?.let {
            when (it.state) {
                ResourceState.LOADING -> {
                    setStatusName("Sending proof")
                    publishProgress("Sending proof")
                }
                ResourceState.SUCCESS -> if (it.data.equals("completed")) notifyAndFinish("Proof was sent") else it.data?.let { it1 -> publishProgress(it1) }
                ResourceState.ERROR -> notifyAndFinish("Sending proof Error: ${it.message}")
            }
        }
    }

    private fun updateProofRequestGot(resource: Resource<ProofRequest>?) {
        resource?.let {
            when (it.state) {
                ResourceState.LOADING -> {
                    setStatusName("Proof request receiving")
                    publishProgress(R.string.progress_accept_invite)
                }
                ResourceState.SUCCESS -> {
                    publishProgress("Proof request received")
                    it.data?.let { it1 -> showDialog(it1) }
                }
                ResourceState.ERROR -> notifyAndFinish("Get Proof request Error: ${it.message}")
            }
        }
    }

    fun showDialog(proofRequest: ProofRequest) {
        val verifier = verifierInfoFromDid("did")
        val requestedData: Set<String> = proofRequest.requestedAttributes.keys + proofRequest.requestedPredicates.keys
        val bodyMessage = formatPopupMessage(verifier, requestedData,
                appState.walletCredentials.value,
                appState.credentialPresentationRules, appState.credentialAttributePresentationRules)

        val dialog = AlertDialog.Builder(this@SimpleScannerActivity)
                .setTitle("Claims Requested")
                .setMessage(bodyMessage)
                .setCancelable(false)
                .setPositiveButton("ALLOW") { _, _ ->
                    publishProgress(R.string.progress_providing_authentication_proofs)
                    vm.sendProof(proofRequest = proofRequest)
                    vm.responseProfSent.observe(this, Observer { updateResponseProfSent(it) })
                }
                .setNegativeButton("CANCEL") { _, _ -> this@SimpleScannerActivity.finish() }
                .create()
        showDialog(dialog)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            val result =  IntentIntegrator.parseActivityResult(requestCode, resultCode, data).contents

            val state = intent?.getStringExtra(EXTRA_STATE)
            if (result == null || !(correctInvite.matches(result) || (PackageState.COLLECTED.name == state && correctUtl.matches(result)))) return
            val content by lazy { SerializationUtils.jSONToAny<Invite>(result) }

            val serial = intent?.getStringExtra(EXTRA_SERIAL)
            collectedAt = intent?.getLongExtra(EXTRA_COLLECTED_AT, 0)

            when (state) {
                PackageState.GETPROOFS.name -> {
                    vm.getCredentials(url = result)
                    vm.responseCredentialsGot.observe(this, Observer { updateResponseCredentialsGot(it) })
                }

                PackageState.NEW.name -> {
                    setStatusName(R.string.state_new)
                    Completable.complete().observeOn(Schedulers.io()).subscribe {
                        try {
                            publishProgress(R.string.progress_accept_invite)
                            agentConnection.acceptInvite(content.invite).toBlocking().value().apply {
                                api.createRequest(AskForPackageRequest(appState.indyState.indyUser.value!!.walletUser.getIdentityDetails().did, content.clientUUID!!)).toBlocking().first()
                                publishProgress(R.string.progress_waiting_for_authentication)
                                val proofRequest = receiveProofRequest().toBlocking().value()
                                val requestedDataBuilder = StringBuilder()
                                publishProgress(R.string.progress_providing_credential_proofs)
                                val requestedData = proofRequest.requestedAttributes.keys + proofRequest.requestedPredicates.keys
                                for (key in requestedData) {
                                    requestedDataBuilder.append(", $key")
                                }
                                getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE).edit().putString(sharedPreferencesKey, requestedDataBuilder.toString().substring(1)).apply()
                                Completable.complete().observeOn(AndroidSchedulers.mainThread()).subscribe {
                                    val dialog = AlertDialog.Builder(this@SimpleScannerActivity)
                                            .setTitle("Claims request")
                                            .setMessage("Treatment center \"TC SEEHOF\" requesting your " + requestedDataBuilder.toString().substring(2) + " to approve your request.Provide it ?")
                                            .setCancelable(false)
                                            .setPositiveButton("PROVIDE") { _, _ ->
                                                Completable.complete().observeOn(Schedulers.io()).subscribe {

                                                    val partyDid = partyDID()
                                                    publishProgress(R.string.progress_providing_authentication_proofs)
                                                    val proofFromLedgerData = appState.indyState.indyUser.value!!.createProofFromLedgerData(proofRequest)
                                                    val connection = agentConnection.getIndyPartyConnection(partyDid).toBlocking().value()
                                                            ?: throw RuntimeException("Agent connection with $partyDid not found")
                                                    connection.sendProof(proofFromLedgerData)

                                                    publishProgress(R.string.progress_receiving_credential_offer)
                                                    val credentialOffer = connection.receiveCredentialOffer().toBlocking().value()

                                                    publishProgress(R.string.progress_creating_credential_request)
                                                    val credentialRequest = appState.indyState.indyUser.value!!
                                                            .createCredentialRequest(appState.indyState.indyUser.value!!.walletUser.getIdentityDetails().did, credentialOffer)
                                                    connection.sendCredentialRequest(credentialRequest)

                                                    publishProgress(R.string.progress_receiving_credential)
                                                    val credential = connection.receiveCredential().toBlocking().value()

                                                    publishProgress(R.string.progress_verifying_credential)
                                                    appState.indyState.indyUser.value!!.checkLedgerAndReceiveCredential(credential, credentialRequest, credentialOffer)

                                                    MainActivity.popupStatus = AtomicInteger(PopupStatus.RECEIVED.ordinal)
                                                    this@SimpleScannerActivity.finish()
                                                }
                                            }
                                            .setNegativeButton("CANCEL") { _, _ -> this@SimpleScannerActivity.finish() }
                                            .create()
                                    showDialog(dialog)
                                }
                            }
                        } catch (er: Exception) {
                            notifyAndFinish("New Package Error: ${er.message}")
                        }
                    }
                }

                PackageState.COLLECTED.name -> {
                    setStatusName(R.string.state_collected)
                    Completable.complete().observeOn(Schedulers.io()).subscribe {
                        try {
                            val retrofit: Retrofit = Retrofit.Builder()
                                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                                    .addConverterFactory(GsonConverterFactory.create(Gson()))
                                    .baseUrl(BASE_URL)
                                    .build()
                            retrofit.client().setReadTimeout(1, TimeUnit.MINUTES)
                            retrofit.create(SovrinAgentService::class.java).packageHistory(Serial(serial!!, null))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        publishProgress(R.string.progress_accept_invite)
                                        agentConnection.acceptInvite(it.invite).toBlocking().value().apply {
                                            publishProgress(R.string.progress_reading_package_history)
                                            val packageCredential = appState.indyState.indyUser.value!!.walletUser.getCredentials().asSequence().find { ref ->
                                                ref.getSchemaIdObject().name.contains("package_receipt") &&
                                                        ref.attributes[EXTRA_SERIAL] == serial
                                            }!!
                                            val authorities =
                                                    SerializationUtils.jSONToAny<AuthorityInfoMap>(packageCredential.attributes[AUTHORITIES].toString())

                                            publishProgress(R.string.progress_waiting_for_authentication)
                                            val proofRequest = receiveProofRequest().toBlocking().value()
                                            val requestedDataBuilder = StringBuilder()
                                            val requestedData = proofRequest.requestedAttributes.keys + proofRequest.requestedPredicates.keys
                                            for (key in requestedData) {
                                                requestedDataBuilder.append(", $key")
                                            }
                                            val dialog = AlertDialog.Builder(this@SimpleScannerActivity)
                                                    .setTitle("Claims request")
                                                    .setMessage("Treatment center \"TC SEEHOF\" requesting your " + requestedDataBuilder.toString().substring(2) + " to approve your request.Provide it ?")
                                                    .setCancelable(false)
                                                    .setPositiveButton("PROVIDE") { _, _ ->
                                                        Completable.complete().observeOn(Schedulers.io()).subscribe {
                                                            MainActivity.popupStatus = AtomicInteger(PopupStatus.IN_PROGRESS.ordinal)
                                                            publishProgress(R.string.progress_providing_authentication_proofs)
                                                            val proofInfo = appState.indyState.indyUser.value!!.createProofFromLedgerData(proofRequest)
                                                            sendProof(proofInfo)
                                                            val provedAuthorities = authorities.mapValues { (_, authority) ->
                                                                val proofRequest = proofRequest("package_history_req", "1.0") {
                                                                    reveal("status") {
                                                                        EXTRA_SERIAL shouldBe serial
                                                                        FilterProperty.IssuerDid shouldBe authority.did
                                                                        FilterProperty.SchemaId shouldBe authority.schemaId
                                                                    }
                                                                    reveal(TIME) {
                                                                        EXTRA_SERIAL shouldBe serial
                                                                        FilterProperty.IssuerDid shouldBe authority.did
                                                                        FilterProperty.SchemaId shouldBe authority.schemaId
                                                                    }
                                                                }
                                                                publishProgress(R.string.progress_requesting_digital_license)
                                                                sendProofRequest(proofRequest)
                                                                publishProgress(R.string.progress_verifying_digital_license)
                                                                val proof = receiveProof().toBlocking().value()
                                                                appState.indyState.indyUser.value!!.verifyProofWithLedgerData(proofRequest, proof)
                                                            }
                                                            Realm.getDefaultInstance().executeTransaction { realm ->
                                                                val productOperation = realm.createObject(ProductOperation::class.java, collectedAt)
                                                                productOperation.by = "approved"
                                                            }
                                                            MainActivity.popupStatus = AtomicInteger(PopupStatus.HISTORY.ordinal)
                                                            this@SimpleScannerActivity.finish()

                                                            Log.e("Passed", "OK")
                                                            //TODO: Add some logic for displaying verification
                                                            saveHistory(Unit)
                                                        }
                                                    }
                                                    .setNegativeButton("CANCEL") { _, _ -> this@SimpleScannerActivity.finish() }
                                                    .create()
                                            showDialog(dialog)
                                        }
                                    }) { er -> notifyAndFinish("Collect Package Error: ${er.message}") }
                        } catch (er: Exception) {
                            notifyAndFinish("New Package Error: ${er.message}")
                        }
                    }
                }

                PackageState.DELIVERED.name -> {
                    setStatusName(R.string.state_delivered)
                    Completable.complete().observeOn(Schedulers.io()).subscribe {
                        try {
                            publishProgress(R.string.progress_accept_invite)
                            agentConnection.acceptInvite(content.invite).toBlocking().value().apply {
                                publishProgress(R.string.progress_collecting_package)
                                api.collectPackage(Serial(serial!!, content.clientUUID!!)).toBlocking().first()

                                publishProgress(R.string.progress_waiting_for_authentication)
                                val proofRequest: ProofRequest = receiveProofRequest().toBlocking().value()

                                publishProgress(R.string.progress_providing_authentication_proofs)
                                val proof = appState.indyState.indyUser.value!!.createProofFromLedgerData(proofRequest)
                                sendProof(proof)
                                Thread.sleep(3000)
                                this@SimpleScannerActivity.finish()
                            }
                        } catch (er: Exception) {
                            notifyAndFinish("Collect Package Invite Error: ${er.message}")
                        }
                    }
                }
                else -> finish()
            }
        } else {
            finish()
        }
    }

    private fun setStatusName(@androidx.annotation.StringRes textId: Int) {
        tvTitle.text = getString(textId)
    }

    private fun setStatusName(string: String) {
        tvTitle.text = string
    }

    private fun publishProgress(@androidx.annotation.StringRes textId: Int) {
        runOnUiThread { tvStatus.text = getString(textId) }
    }

    private fun publishProgress(s: String) {
        runOnUiThread { tvStatus.text = s }
    }

    private fun notifyAndFinish(@StringRes textId: Int) {
        notifyAndFinish(getString(textId))
    }

    private fun notifyAndFinish(text: String) {
        runOnUiThread { Toast.makeText(this, text, Toast.LENGTH_LONG).show() }
        finish()
    }

    private fun showDialog(dialog: AlertDialog) {
        runOnUiThread { dialog.show() }
    }

    private fun saveHistory(it: Unit?) {
        Realm.getDefaultInstance().executeTransaction {
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun formatPopupMessage(
            verifier: VerifierInfo,
            requestedAttributeKeys: Set<String>,
            creds: List<CredentialReference>?,
            credentialPresentationRules: CredentialPresentationRules,
            attributePresentationRules: CredentialAttributePresentationRules
    ): String {
        val requestedClaims = requestedAttributeKeys
                .map { attributePresentationRules.formatName(it) }
                .joinToString(separator = "\n  - ", prefix = "  - ")

        val requestedCredentials = creds!!
                .filter { it.attributes.keys.intersect(requestedAttributeKeys).isNotEmpty() }
                .map { credentialPresentationRules.formatName(it) }
                .joinToStringPrettyAnd()

        return """
        |${verifier.name} is requesting your $requestedCredentials credentials.
        |
        |The following claims will be revealed:
        |${requestedClaims} 
    """.trimMargin()
    }

    /**
     * Joins a list to string: "el1, el2, el3 and el4"
     * */
    fun <T> List<T>.joinToStringPrettyAnd() =
            this.dropLast(1).joinToString(separator = ", ") + " and " + this.last().toString()
}
