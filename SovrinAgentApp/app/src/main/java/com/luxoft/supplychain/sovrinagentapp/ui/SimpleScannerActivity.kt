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

package com.luxoft.supplychain.sovrinagentapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection
import com.luxoft.blockchainlab.hyperledger.indy.IndyUser
import com.luxoft.blockchainlab.hyperledger.indy.models.ProofRequest
import com.luxoft.blockchainlab.hyperledger.indy.utils.SerializationUtils
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.communcations.SovrinAgentService
import com.luxoft.supplychain.sovrinagentapp.data.AskForPackageRequest
import com.luxoft.supplychain.sovrinagentapp.data.Invite
import com.luxoft.supplychain.sovrinagentapp.data.PackageState
import com.luxoft.supplychain.sovrinagentapp.data.Serial
import com.luxoft.supplychain.sovrinagentapp.di.updateCredentialsInRealm
import com.luxoft.supplychain.sovrinagentapp.ui.MainActivity.Companion.showAlertDialog
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView
import org.koin.android.ext.android.inject
import rx.Completable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class SimpleScannerActivity : AppCompatActivity(), ZBarScannerView.ResultHandler {

    private val requestCode = 117
    private var mScannerView: ZBarScannerView? = null
    private val api: SovrinAgentService by inject()
    private val indyUser: IndyUser by inject()
    private val agentConnection: AgentConnection by inject()


    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_simple_scanner)
        setupToolbar()

        val contentFrame = findViewById<ViewGroup>(R.id.content_frame)
        mScannerView = ZBarScannerView(this)
        contentFrame.addView(mScannerView)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == this.requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Everything is peachy
            } else {
                Toast.makeText(this, "Camera permission denied. Barcode scanning will not work", Toast.LENGTH_LONG).show()
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), requestCode)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mScannerView?.setResultHandler(this)
        mScannerView?.startCamera()
//        handleResult(Result().apply {
//            contents =
//                    ""
//        })
    }

    override fun onPause() {
        super.onPause()
        mScannerView?.stopCamera()
    }

    val correct = Regex(".+\\/indy\\?c_i=.+")

    override fun handleResult(rawResult: Result) {
        if (rawResult.contents == null || !correct.matches(rawResult.contents)) return

        val content: Invite = SerializationUtils.jSONToAny(rawResult.contents)

        mScannerView!!.stopCamera()
        drawProgressBar()

        val state = intent?.getStringExtra("state")
        val serial = intent?.getStringExtra("serial")

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

                            ContextCompat.startActivity(
                                    this@SimpleScannerActivity,
                                    Intent().setClass(this@SimpleScannerActivity, AskClaimsActivity::class.java)
                                            .putExtra("result", rawResult.contents)
                                            .putExtra("proofRequest", SerializationUtils.anyToJSON(proofRequest))
                                            .putExtra("partyDID", partyDID())
                                            .putExtra("serial", intent?.getStringExtra("serial")),
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

        // Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the time to figure out.
//        val handler = Handler()
//        handler.postDelayed({ mScannerView!!.resumeCameraPreview(this@SimpleScannerActivity) }, 2000)
    }

    private fun setupToolbar() {
//        val toolbar = findViewById<Toolbar>(R.id.toolbar)
//        setSupportActionBar(toolbar)
//        val ab = supportActionBar
//        ab?.setDisplayHomeAsUpEnabled(true)
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
