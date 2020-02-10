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

package com.luxoft.supplychain.sovrinagentapp.ui.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.blikoon.qrcodescanner.QrCodeActivity
import com.fasterxml.jackson.databind.JsonMappingException
import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection
import com.luxoft.blockchainlab.hyperledger.indy.models.CredentialReference
import com.luxoft.blockchainlab.hyperledger.indy.models.ProofInfo
import com.luxoft.blockchainlab.hyperledger.indy.utils.SerializationUtils
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.application.EXTRA_COLLECTED_AT
import com.luxoft.supplychain.sovrinagentapp.application.EXTRA_STATE
import com.luxoft.supplychain.sovrinagentapp.application.QR_SCANNER_CODE_EXTRA
import com.luxoft.supplychain.sovrinagentapp.data.*
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_scanner.*
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import rx.Completable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class SimpleScannerActivity : AppCompatActivity() {

    private val appState: ApplicationState by inject()

    private val agentConnection: AgentConnection by inject()
    private val requestCodeScan = 101

    private val correctInvite = Regex(".+\\/indy\\?c_i=.+")
    private val correctUtl = Regex("http:\\/\\/.+mf\\/package\\/history")
    private var collectedAt: Long? = 0

    private val sharedPreferencesName = "REQUESTED_DATA_SP"
    private val sharedPreferencesKey = "REQUESTED_DATA_KEY"

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_scanner)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_back)
        }

        startActivityForResult(Intent(this@SimpleScannerActivity, QrCodeActivity::class.java), requestCodeScan)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == requestCodeScan) {

            if (resultCode == Activity.RESULT_OK) {
                val result = data?.getStringExtra(QR_SCANNER_CODE_EXTRA)

                val state = intent?.getStringExtra(EXTRA_STATE)
                if (result == null || !(correctInvite.matches(result) || (PackageState.COLLECTED.name == state && correctUtl.matches(result)))) return

                collectedAt = intent?.getLongExtra(EXTRA_COLLECTED_AT, 0)

                when (state) {
                    PackageState.GETPROOFS.name -> {
                        setStatusName(R.string.state_get_proofs)
                        Completable.complete().observeOn(Schedulers.io()).subscribe {
                            val parsedInvite = try {
                                publishProgress(R.string.progress_parse_qr_code)
                                SerializationUtils.jSONToAny<Invite>(result)
                            } catch (error: JsonMappingException) {
                                notifyErrorAndFinish(error, "This is NOT a new Credential QR code!",
                                    "Please make sure to scan an appropriate QR code")
                                return@subscribe
                            }catch (error: Throwable) {
                                notifyErrorAndFinish(error, "Failed to process QR code for a new Credential.",
                                    "Please try refreshing the website.")
                                return@subscribe
                            }

                            try {
                                publishProgress(R.string.progress_accept_invite)
                                agentConnection.acceptInvite(parsedInvite.invite).toBlocking().value().apply {
                                    publishProgress(R.string.progress_receiving_credential)
                                    do {
                                        val credOffer = try {
                                            receiveCredentialOffer().timeout(5, TimeUnit.SECONDS).toBlocking().value()
                                        } catch (e: RuntimeException) {
                                            //End of waiting for new credentials
                                            if (e.cause !is TimeoutException)
                                                throw e
                                            null
                                        }?.apply {
                                            val indyUser = appState.indyState.indyUser.value!!
                                            val credentialRequest = indyUser.createCredentialRequest(indyUser.walletUser.getIdentityDetails().did, this)
                                            sendCredentialRequest(credentialRequest)
                                            val credential = receiveCredential().toBlocking().value()
                                            publishProgress(R.string.progress_verifying_credential)
                                            indyUser.checkLedgerAndReceiveCredential(credential, credentialRequest, this)
                                        }
                                    } while (credOffer != null)

                                    appState.updateWalletCredentials()
                                    notifyAndFinish(R.string.progress_state_get_proofs_finished)
                                }
                            } catch (er: Exception) {
                                notifyErrorAndFinish(er, "Failed to receive a new Credential", "Please check that servers are OK")
                            }
                        }
                    }

                    PackageState.NEW.name -> {
                        setStatusName(R.string.state_new)
                        Completable.complete().observeOn(Schedulers.io()).subscribe {
                            try {
                                publishProgress(R.string.progress_accept_invite)
                                agentConnection.acceptInvite(result).toBlocking().value().apply {
                                    publishProgress(R.string.progress_waiting_for_authentication)
                                    val proofRequest = receiveProofRequest().toBlocking().value()
                                    publishProgress(R.string.progress_providing_credential_proofs)
                                    val requestedData: Set<String> = proofRequest.requestedAttributes.keys + proofRequest.requestedPredicates.keys
                                    val requestedDataStr = requestedData.joinToString(separator = ", ")

                                    getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE).edit().putString(sharedPreferencesKey, requestedDataStr).apply()
                                    Completable.complete().observeOn(AndroidSchedulers.mainThread()).subscribe {
                                        val verifier = verifierInfoFromDid(partyDID())

                                        val bodyMessage = formatPopupMessage(verifier, requestedData, appState.walletCredentials.value)

                                        val dialog = AlertDialog.Builder(this@SimpleScannerActivity)
                                            .setTitle("Claims Requested")
                                            .setMessage(bodyMessage)
                                            .setCancelable(false)
                                            .setPositiveButton("ALLOW") { _, _ ->
                                                Completable.complete().observeOn(Schedulers.io()).subscribe {

                                                    publishProgress(R.string.progress_providing_authentication_proofs)
                                                    val proofFromLedgerData: ProofInfo = appState.indyState.indyUser.value!!.createProofFromLedgerData(proofRequest)
                                                    val connection = agentConnection.getIndyPartyConnection(verifier.did).toBlocking().value()
                                                            ?: throw RuntimeException("Agent connection with ${verifier.did} not found")
                                                    connection.sendProof(proofFromLedgerData)

                                                    val event = VerificationEvent(
                                                            Instant.now(),
                                                            proofFromLedgerData,
                                                            proofRequest,
                                                            requestedData,
                                                            verifier)

                                                    appState.storeVerificationEvent(event)

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


                    else -> finish()
                }
            } else {
                finish()
            }
        }
    }

    private fun setStatusName(@StringRes textId: Int) {
        tvTitle.text = getString(textId)
    }

    private fun publishProgress(@StringRes textId: Int) {
        runOnUiThread { tvStatus.text = getString(textId) }
    }

    private fun notifyAndFinish(@StringRes textId: Int) {
        notifyAndFinish(getString(textId))
    }

    private fun notifyAndFinish(text: String) {
        runOnUiThread { Toast.makeText(this, text, Toast.LENGTH_LONG).show() }
        finish()
    }

    private fun notifyErrorAndFinish(error: Throwable,
                                     description: String,
                                     message: String) {
        notifyAndFinish("""
            |$description 
            |$message
            |
            |$error
            """.trimMargin())
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
        creds: List<CredentialReference>?
    ): String {
        val credentialPresentationRules: CredentialPresentationRules = get()
        val attributePresentationRules: CredentialAttributePresentationRules = get()

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

}

/**
 * Joins a list to string: "el1, el2, el3 and el4"
* */
fun <T> List<T>.joinToStringPrettyAnd() =
    this.dropLast(1).joinToString(separator = ", ") + " and " + this.last().toString()
