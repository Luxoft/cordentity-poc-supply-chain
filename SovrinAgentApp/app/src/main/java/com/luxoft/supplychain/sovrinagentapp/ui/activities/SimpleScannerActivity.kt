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
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import com.blikoon.qrcodescanner.QrCodeActivity
import com.google.gson.Gson
import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection
import com.luxoft.blockchainlab.hyperledger.indy.IndyUser
import com.luxoft.blockchainlab.hyperledger.indy.models.ProofRequest
import com.luxoft.blockchainlab.hyperledger.indy.utils.FilterProperty
import com.luxoft.blockchainlab.hyperledger.indy.utils.SerializationUtils
import com.luxoft.blockchainlab.hyperledger.indy.utils.proofRequest
import com.luxoft.blockchainlab.hyperledger.indy.utils.reveal
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.application.*
import com.luxoft.supplychain.sovrinagentapp.communcations.SovrinAgentService
import com.luxoft.supplychain.sovrinagentapp.data.*
import com.luxoft.supplychain.sovrinagentapp.ui.activities.MainActivity.Companion.showAlertDialog
import com.luxoft.supplychain.sovrinagentapp.utils.updateCredentialsInRealm
import io.realm.Realm
import me.dm7.barcodescanner.zbar.ZBarScannerView
import org.koin.android.ext.android.inject
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

    private var mScannerView: ZBarScannerView? = null
    private val api: SovrinAgentService by inject()
    private val indyUser: IndyUser by inject()
    private val agentConnection: AgentConnection by inject()
    private val requestCodeScan = 101

    private val correctInvite = Regex(".+\\/indy\\?c_i=.+")
    private val correctUtl = Regex("http:\\/\\/.+mf\\/package\\/history")
    private var collectedAt: Long? = 0

    private val sharedPreferencesName = "REQUESTED_DATA_SP"
    private val sharedPreferencesKey = "REQUESTED_DATA_KEY"

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        mScannerView = ZBarScannerView(this)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_back)
        }

        startActivityForResult(Intent(this@SimpleScannerActivity, QrCodeActivity::class.java), requestCodeScan)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == requestCodeScan) {
            if (resultCode == Activity.RESULT_OK) {
                val result = data?.getStringExtra(QR_SCANNER_CODE_EXTRA)

                val state = intent?.getStringExtra(EXTRA_STATE)
                if (result == null || !(correctInvite.matches(result) || (PackageState.COLLECTED.name == state && correctUtl.matches(result)))) return
                val content by lazy { SerializationUtils.jSONToAny<Invite>(result) }

                mScannerView!!.stopCamera()

                val serial = intent?.getStringExtra(EXTRA_SERIAL)
                collectedAt = intent?.getLongExtra("collected_at", 0)

                when (state) {
                    PackageState.GETPROOFS.name -> {
                        Completable.complete().observeOn(Schedulers.io()).subscribe {
                            try {
                                agentConnection.acceptInvite(content.invite).toBlocking().value().apply {
                                    do {
                                        val credOffer = try {
                                            receiveCredentialOffer().timeout(5, TimeUnit.SECONDS).toBlocking().value()
                                        } catch (e: RuntimeException) {
                                            //End of waiting for new credentials
                                            if (e.cause !is TimeoutException)
                                                throw e
                                            null
                                        }?.apply {
                                            val credentialRequest = indyUser.createCredentialRequest(indyUser.walletUser.getIdentityDetails().did, this)
                                            sendCredentialRequest(credentialRequest)
                                            val credential = receiveCredential().toBlocking().value()
                                            indyUser.checkLedgerAndReceiveCredential(credential, credentialRequest, this)
                                        }
                                    } while (credOffer != null)
                                    indyUser.walletUser.updateCredentialsInRealm()
                                    finish()
                                }
                            } catch (er: Exception) {
                                Log.e("Get Claims Error: ", er.message, er)
                                showAlertDialog(baseContext, "Get Claims Error: ${er.message}") { finish() }
                            }
                        }
                    }

                    PackageState.NEW.name -> {
                        Completable.complete().observeOn(Schedulers.io()).subscribe {
                            try {
                                agentConnection.acceptInvite(content.invite).toBlocking().value().apply {
                                    api.createRequest(AskForPackageRequest(indyUser.walletUser.getIdentityDetails().did, content.clientUUID!!)).toBlocking().first()
                                    val proofRequest = receiveProofRequest().toBlocking().value()
                                    var requestedDataBuilder = StringBuilder()
                                    val requestedData = proofRequest.requestedAttributes.keys + proofRequest.requestedPredicates.keys
                                    for (key in requestedData) {
                                        requestedDataBuilder.append(", $key")
                                    }
                                    getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE).edit().putString(sharedPreferencesKey, requestedDataBuilder.toString().substring(1)).apply()
                                    ContextCompat.startActivity(
                                        this@SimpleScannerActivity,
                                        Intent().setClass(this@SimpleScannerActivity, AskClaimsActivity::class.java)
                                            .putExtra("result", result)
                                            .putExtra("proofRequest", SerializationUtils.anyToJSON(proofRequest))
                                            .putExtra("partyDID", partyDID())
                                            .putExtra(EXTRA_SERIAL, intent?.getStringExtra(EXTRA_SERIAL)),
                                        null
                                    )

                                    finish()
                                }
                            } catch (er: Exception) {
                                Log.e("New Package Error: ", er.message, er)
                                showAlertDialog(baseContext, "New Package Error: ${er.message}") { finish() }
                            }
                        }
                    }

                    PackageState.COLLECTED.name -> {
                        Completable.complete().observeOn(Schedulers.io()).subscribe {
                            try {
                                val retrofit: Retrofit = Retrofit.Builder()
                                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                                    .addConverterFactory(GsonConverterFactory.create(Gson()))
                                    .baseUrl(result)
                                    .build()
                                retrofit.client().setReadTimeout(1, TimeUnit.MINUTES)
                                retrofit.create(SovrinAgentService::class.java).packageHistory(Serial(serial!!, null), "")
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        agentConnection.acceptInvite(it.invite).toBlocking().value().apply {
                                            val packageCredential = indyUser.walletUser.getCredentials().asSequence().find {
                                                it.getSchemaIdObject().name.contains("package_receipt") &&
                                                    it.attributes[EXTRA_SERIAL] == serial
                                            }!!
                                            val authorities =
                                                SerializationUtils.jSONToAny<AuthorityInfoMap>(packageCredential.attributes[AUTHORITIES].toString())

                                            val proofRequest = receiveProofRequest().toBlocking().value()
                                            val requestedDataBuilder = StringBuilder()
                                            val requestedData = proofRequest.requestedAttributes.keys + proofRequest.requestedPredicates.keys
                                            for (key in requestedData) {
                                                requestedDataBuilder.append(", $key")
                                            }
                                            AlertDialog.Builder(this@SimpleScannerActivity)
                                                .setTitle("Claims request")
                                                .setMessage("Treatment center \"TC SEEHOF\" requesting your " + requestedDataBuilder.toString().substring(2) + " to approve your request.Provide it ?")
                                                .setCancelable(false)
                                                .setPositiveButton("PROVIDE", object : DialogInterface.OnClickListener {
                                                    override fun onClick(dialog: DialogInterface, which: Int) {
                                                        Completable.complete().observeOn(Schedulers.io()).subscribe {
                                                            MainActivity.popupStatus = AtomicInteger(1)
                                                            this@SimpleScannerActivity.finish()
                                                            val proofInfo = indyUser.createProofFromLedgerData(proofRequest)
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
                                                                sendProofRequest(proofRequest)
                                                                val proof = receiveProof().toBlocking().value()
                                                                indyUser.verifyProofWithLedgerData(proofRequest, proof)
                                                            }
                                                            Realm.getDefaultInstance().executeTransaction { realm ->
                                                                val productOperation = realm.createObject(ProductOperation::class.java, collectedAt)
                                                                productOperation.by = "approved"
                                                            }
                                                            MainActivity.popupStatus = AtomicInteger(3)

                                                            Log.e("Passed", "OK")
                                                            //TODO: Add some logic for displaying verification
                                                            saveHistory(Unit)
                                                        }
                                                    }
                                                })
                                                .setNegativeButton("CANCEL", object : DialogInterface.OnClickListener {
                                                    override fun onClick(dialog: DialogInterface, which: Int) {
                                                        this@SimpleScannerActivity.finish()
                                                    }
                                                })
                                                .create()
                                                .show()
                                        }
                                    }) { er ->
                                        Log.e("Collect Package Error: ", er.message, er)
//                                            showAlertDialog(baseContext, "Collect Package Error: ${er.message}") { finish() }
                                        this@SimpleScannerActivity.finish()
                                    }
                            } catch (er: Exception) {
                                Log.e("New Package Error: ", er.message, er)
                                showAlertDialog(baseContext, "New Package Error: ${er.message}") { finish() }
                            }
                        }

                    }

                    PackageState.DELIVERED.name -> {
                        Completable.complete().observeOn(Schedulers.io()).subscribe {
                            try {
                                agentConnection.acceptInvite(content.invite).toBlocking().value().apply {
                                    api.collectPackage(Serial(serial!!, content.clientUUID!!))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe({
                                            // TODO: this call should return immediately
                                            // TODO: after this you should listen to new ingoing proof request
                                            // TODO: when proof request is received you should show a popup with something like "Treatment Center wants you to prove token ownership, agree?"
                                            // TODO: if agree you should generate proof out of the proof request and send it back
                                            // TODO: only if the proof is valid Corda-side should commit transaction


                                            val proofRequest: ProofRequest = receiveProofRequest().toBlocking().value()
                                            val proof = indyUser.createProofFromLedgerData(proofRequest)
                                            sendProof(proof)
                                            Thread.sleep(3000)
                                            finish()
                                        }) { er ->
                                            Log.e("Collect Package Error: ", er.message, er)
                                            showAlertDialog(baseContext, "Collect Package Error: ${er.message}") { finish() }
                                        }
                                }
                            } catch (er: Exception) {
                                Log.e("Collect Package Invite Error: ", er.message, er)
                                showAlertDialog(baseContext, "Collect Package Invite GError: ${er.message}") { finish() }
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
}
