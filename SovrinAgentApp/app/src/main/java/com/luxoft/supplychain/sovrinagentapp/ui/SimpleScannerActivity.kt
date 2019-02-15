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
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.communcations.SovrinAgentService
import com.luxoft.supplychain.sovrinagentapp.data.PackageState
import com.luxoft.supplychain.sovrinagentapp.data.Serial
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView
import org.koin.android.ext.android.inject
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class SimpleScannerActivity : AppCompatActivity(), ZBarScannerView.ResultHandler {

    private val requestCode = 117
    private var mScannerView: ZBarScannerView? = null
    private val api: SovrinAgentService by inject()


    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_simple_scanner)
        setupToolbar()

        val contentFrame = findViewById<ViewGroup>(R.id.content_frame)
        mScannerView = ZBarScannerView(this)
        contentFrame.addView(mScannerView)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,  arrayOf(Manifest.permission.CAMERA), requestCode)
        }
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
                    ActivityCompat.requestPermissions(this,  arrayOf(Manifest.permission.CAMERA), requestCode)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mScannerView?.setResultHandler(this)
        mScannerView?.startCamera()
    }

    override fun onPause() {
        super.onPause()
        mScannerView?.stopCamera()
    }

    override fun handleResult(rawResult: Result) {

        val state = intent?.getStringExtra("state")
        val serial = intent?.getStringExtra("serial")

        when (state) {
            PackageState.NEW.name -> {

                // TODO: start new connection if there is no any using qr code content (there should be invite)

                ContextCompat.startActivity(
                        this,
                        Intent().setClass(this, AskClaimsActivity::class.java)
                                .putExtra("result", rawResult.contents)
                                .putExtra("serial", intent?.getStringExtra("serial")),
                        null
                )
                finish()
            }

            PackageState.DELIVERED.name -> {

                ContextCompat.startActivity(
                        this,
                        Intent().setClass(this, MainActivity::class.java),
                        null
                )

                api.collectPackage(Serial(serial!!)).subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            // TODO: this call should return immediately
                            // TODO: after this you should listen to new ingoing proof request
                            // TODO: when proof request is received you should show a popup with something like "Treatment Center wants you to prove token ownership, agree?"
                            // TODO: if agree you should generate proof out of the proof request and send it back
                            // TODO: only if the proof is valid Corda-side should commit transaction

                            finish()
                        }) { error ->
                            Log.e("", error.message)
                            finish()
                        }

                ContextCompat.startActivity(
                        this,
                        Intent().setClass(this, MainActivity::class.java)
                                .putExtra("result", rawResult.contents)
                                .putExtra("serial", intent?.getStringExtra("serial")),
                        null
                )
                finish()
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
