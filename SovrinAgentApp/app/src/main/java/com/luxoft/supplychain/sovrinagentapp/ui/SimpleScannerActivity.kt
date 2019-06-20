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
import com.blikoon.qrcodescanner.QrCodeActivity
import android.content.DialogInterface
import android.content.DialogInterface.BUTTON_NEUTRAL
import android.app.Activity
import android.app.AlertDialog
import com.google.gson.Gson
import com.luxoft.supplychain.sovrinagentapp.Application.Companion.webServerEndpoint
import com.luxoft.supplychain.sovrinagentapp.data.*
import io.realm.Realm

class SimpleScannerActivity : AppCompatActivity(), ZBarScannerView.ResultHandler {

    private val requestCode = 117
    private var mScannerView: ZBarScannerView? = null
    private val api: SovrinAgentService by inject()
    private val indyUser: IndyUser by inject()
    private val agentConnection: AgentConnection by inject()
    private val REQUEST_CODE_QR_SCAN = 101


    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
//        setContentView(R.layout.activity_simple_scanner)
        setupToolbar()

//        val contentFrame = findViewById<ViewGroup>(R.id.content_frame)
        mScannerView = ZBarScannerView(this)
//        contentFrame.addView(mScannerView)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)

        val i = Intent(this@SimpleScannerActivity, QrCodeActivity::class.java)
        startActivityForResult(i, REQUEST_CODE_QR_SCAN)
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
//        mScannerView?.setResultHandler(this)
//        mScannerView?.startCamera()
//        handleResult(Result().apply {
//            contents =
//                    "{\"invite\":\"http://172.17.0.3:8095/indy?c_i=eyJAdHlwZSI6ICJkaWQ6c292OkJ6Q2JzTlloTXJqSGlxWkRUVUFTSGc7c3BlYy9jb25uZWN0aW9ucy8xLjAvaW52aXRhdGlvbiIsICJsYWJlbCI6ICJ0cmVhdG1lbnRDZW50ZXIiLCAicmVjaXBpZW50S2V5cyI6IFsiSjY2dUp0RVVCMzkxOXB5aTZxUWYydDZTNFdwNkFiMXFnU01aalc3YzV1Q2oiXSwgInNlcnZpY2VFbmRwb2ludCI6ICJodHRwOi8vMTcyLjE3LjAuMzo4MDk1L2luZHkiLCAiQGlkIjogIjA2MjhlMjFhLWJmZGItNDEzNy05OGVjLTE2ZWVhMTIzZDdkOSJ9\",\"clientUUID\":\"7170c4d8-0477-44b0-86a7-463209e22b00\"}"        })
    }

    override fun onPause() {
        super.onPause()
//        mScannerView?.stopCamera()
    }

    val correct = Regex(".+\\/indy\\?c_i=.+")
    var collectedAt: Long? = 0

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_QR_SCAN) {
            val result = data?.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult")


            if (result == null || !correct.matches(result)) return

//        val content: Invite = Invite("http://172.17.0.3:8095/indy?c_i=eyJAdHlwZSI6ICJkaWQ6c292OkJ6Q2JzTlloTXJqSGlxWkRUVUFTSGc7c3BlYy9jb25uZWN0aW9ucy8xLjAvaW52aXRhdGlvbiIsICJsYWJlbCI6ICJ0cmVhdG1lbnRDZW50ZXIiLCAicmVjaXBpZW50S2V5cyI6IFsiSjY2dUp0RVVCMzkxOXB5aTZxUWYydDZTNFdwNkFiMXFnU01aalc3YzV1Q2oiXSwgInNlcnZpY2VFbmRwb2ludCI6ICJodHRwOi8vMTcyLjE3LjAuMzo4MDk1L2luZHkiLCAiQGlkIjogIjA2MjhlMjFhLWJmZGItNDEzNy05OGVjLTE2ZWVhMTIzZDdkOSJ9", "7170c4d8-0477-44b0-86a7-463209e22b00")
            val content: Invite = SerializationUtils.jSONToAny(result)

            mScannerView!!.stopCamera()
            drawProgressBar()

            val state = intent?.getStringExtra("state")
            val serial = intent?.getStringExtra("serial")
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

                                ContextCompat.startActivity(
                                        this@SimpleScannerActivity,
                                        Intent().setClass(this@SimpleScannerActivity, AskClaimsActivity::class.java)
                                                .putExtra("result", result)
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
                PackageState.COLLECTED.name -> {
//                    val gson: Gson = Gson()
//                    webServerEndpoint = ""
//                    val disposableWebServerEndpoint = (gson.fromJson(result, HashMap<String, String>()::class.java) as HashMap).getValue("invite")
//                    Completable.complete().observeOn(Schedulers.io()).subscribe {
//                        try {
//                            agentConnection.acceptInvite(content.invite).toBlocking().value().apply {
                                showAlertDialogToProvide()
//                                api.packageHistory(Serial(serial!!, content.clientUUID!!), disposableWebServerEndpoint)
//                                        .subscribeOn(Schedulers.io())
//                                        .observeOn(AndroidSchedulers.mainThread())
//                                        .subscribe({
//                                            webServerEndpoint = "http://18.216.169.252:8082"
//
//                                            saveHistory(it)
//                                            finish()
//                                        }) { er ->
//                                            webServerEndpoint = "http://18.216.169.252:8082"
//                                            Log.e("Collect Package Error: ", er.message, er)
////                                            showAlertDialog(baseContext, "Collect Package Error: ${er.message}") { finish() }
//                                            this@SimpleScannerActivity.finish()
//                                        }
//                            }
//                        } catch (er: Exception) {
////                            webServerEndpoint = "http://18.216.169.252:8082"
//                            Log.e("New Package Error: ", er.message, er)
//                            showAlertDialog(baseContext, "New Package Error: ${er.message}") { finish() }
//                        }
//                    }
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

        }
    }

    fun showAlertDialogToProvide() {
        runOnUiThread(Runnable {
            AlertDialog.Builder(this)
                    .setTitle("Claims request")
                    .setMessage("Treatment center \"TC SEEHOF\" requesting your Full Name, Date of Birth and Address to approve your request. Provide it?")
                    .setCancelable(false)
                    .setPositiveButton("PROVIDE", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, which: Int) {
                            Realm.getDefaultInstance().executeTransaction {
                                val productOperation = it.createObject(ProductOperation::class.java, collectedAt)
                                productOperation.by = "operated"
                            }
                            this@SimpleScannerActivity.finish()
                        }
                    })
                    .setNegativeButton("CANCEL", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, which: Int) {
                            this@SimpleScannerActivity.finish()
                        }
                    })
                    .create()
                    .show()
        })
    }

    private fun saveHistory(it: Unit?) {
        Realm.getDefaultInstance().executeTransaction {
        }
    }

    override fun handleResult(rawResult: Result) {
        if (rawResult.contents == null || !correct.matches(rawResult.contents)) return

//        val content: Invite = Invite("http://172.17.0.3:8095/indy?c_i=eyJAdHlwZSI6ICJkaWQ6c292OkJ6Q2JzTlloTXJqSGlxWkRUVUFTSGc7c3BlYy9jb25uZWN0aW9ucy8xLjAvaW52aXRhdGlvbiIsICJsYWJlbCI6ICJ0cmVhdG1lbnRDZW50ZXIiLCAicmVjaXBpZW50S2V5cyI6IFsiSjY2dUp0RVVCMzkxOXB5aTZxUWYydDZTNFdwNkFiMXFnU01aalc3YzV1Q2oiXSwgInNlcnZpY2VFbmRwb2ludCI6ICJodHRwOi8vMTcyLjE3LjAuMzo4MDk1L2luZHkiLCAiQGlkIjogIjA2MjhlMjFhLWJmZGItNDEzNy05OGVjLTE2ZWVhMTIzZDdkOSJ9", "7170c4d8-0477-44b0-86a7-463209e22b00")
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
