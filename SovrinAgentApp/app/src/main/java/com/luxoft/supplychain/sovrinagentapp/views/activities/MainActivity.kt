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

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.luxoft.blockchainlab.corda.hyperledger.indy.IndyPartyConnection
import com.luxoft.blockchainlab.hyperledger.indy.IndyUser
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.application.FIELD_KEY
import com.luxoft.supplychain.sovrinagentapp.application.NAME
import com.luxoft.supplychain.sovrinagentapp.data.ApplicationState
import com.luxoft.supplychain.sovrinagentapp.data.ClaimAttribute
import com.luxoft.supplychain.sovrinagentapp.data.PopupStatus
import com.luxoft.supplychain.sovrinagentapp.utils.Resource
import com.luxoft.supplychain.sovrinagentapp.utils.ResourceState
import com.luxoft.supplychain.sovrinagentapp.views.adapters.ViewPagerAdapter
import com.luxoft.supplychain.sovrinagentapp.views.fragments.ClaimsFragment
import com.luxoft.supplychain.sovrinagentapp.views.fragments.HistoryFragment
import com.luxoft.supplychain.sovrinagentapp.views.fragments.OrdersFragment
import com.luxoft.supplychain.sovrinagentapp.utils.showNotification
import com.luxoft.supplychain.sovrinagentapp.utils.updateCredentialsInRealm
import com.luxoft.supplychain.sovrinagentapp.viewmodel.IndyViewModel
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.viewModel
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : AppCompatActivity() {

    //    private val realm: Realm = Realm.getDefaultInstance()
    private val appState: ApplicationState by inject()
//    private lateinit var ordersFragment: OrdersFragment
    private val vm: IndyViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")
        System.setProperty("jna.debug_load", "true")

        appState.indyState.indyUser.observeForever { user: IndyUser ->
            user.walletUser.updateCredentialsInRealm()
        }

        setContentView(R.layout.activity_main)
//        setupToolbar()
//        setupViewPager()

//        startTimer()
        appState.user.observe({lifecycle}) { user ->
        }
        getQr()
    }

//    private fun setupViewPager() {
//        val adapter = ViewPagerAdapter(supportFragmentManager)
//        adapter.addFrag(ClaimsFragment())
//        ordersFragment = OrdersFragment()
//        adapter.addFrag(ordersFragment)
//        adapter.addFrag(HistoryFragment())
//        viewpager.adapter = adapter
//
//        tabs.setupWithViewPager(viewpager)
//    }

//    private fun setupToolbar() {
//        appState.indyState.indyUser.observeForever { user: IndyUser ->
//            user.walletUser.updateCredentialsInRealm()
//        }
//        val nameClaims = realm.where(ClaimAttribute::class.java)
//                .equalTo(FIELD_KEY, NAME)
//                .findAllAsync()
//
//        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(false)
//
////        nameClaims.addChangeListener { claims ->
////            val userName = claims.first()?.value ?: ""
////            supportActionBar?.title = userName
////        }
//        appState.user.observe({ lifecycle }) { user ->
//            headerTitle.text = user.name ?: ""
//        }
//    }

//    override fun onDestroy() {
//        super.onDestroy()
//        realm.removeAllChangeListeners()
//        realm.close()
//    }

    companion object {
        var popupStatus: AtomicInteger = AtomicInteger(0)
        var inProgress: Boolean = false

        //TODO to toast
        fun showAlertDialog(context: Context, cause: String?, callback: () -> Unit = {}) = AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage(cause)
                .setCancelable(false)
                .setPositiveButton("ok") { _, _ -> callback() }.show()
    }

//    var uiHandler: Handler? = null
//    lateinit var myTimer: Timer
//    val timeoutRunnable = Runnable {
//        when (popupStatus.get()) {
//            PopupStatus.IN_PROGRESS.ordinal -> {
//                if (!inProgress) {
//                    showNotification(this, "In progress", "")
//                    inProgress = true
//                }
//            }
//            PopupStatus.RECEIVED.ordinal -> {
//                if (inProgress) {
//                    inProgress = false
//                    showNotification(this, getString(R.string.new_digital_receipt), getString(R.string.you_ve_received))
//                    ordersFragment.onResume()
//                }
//            }
//            PopupStatus.HISTORY.ordinal -> {
//                if (inProgress) {
//                    inProgress = false
//                    showNotification(this, "Package history", "Your package history is available")
//                }
//            }
//        }
//    }

//    private val timeoutTimerTask = object : TimerTask() {
//        override fun run() {
//            uiHandler!!.post(timeoutRunnable)
//        }
//    }
//
//    private fun startTimer() {
//        myTimer = Timer()
//        uiHandler = Handler()
//        myTimer.schedule(timeoutTimerTask, 1L * 1000, 1L * 1000)
//    }

    fun getQr() {
        vm.getInviteQRCode()
        vm.qrCode.observe(this, Observer { updateQRCode(it) })
    }

    private fun updateQRCode(resource: Resource<Bitmap>?) {
        resource?.let {
            when (it.state) {
                ResourceState.LOADING -> {
                    Toast.makeText(this, "Creating invite QR code", Toast.LENGTH_LONG)
                }
                ResourceState.SUCCESS -> {
                    Toast.makeText(this, "QR code is created", Toast.LENGTH_LONG)
                    showQR(it.data)
                }
                ResourceState.ERROR -> Toast.makeText(this, "Get Claims Error: ${it.message}", Toast.LENGTH_LONG)
            }
        }
    }

    private fun showQR(bitmap: Bitmap?) {
//        imageViewQRCode.setImageBitmap(bitmap)
        vm.waitForInvitedParty(30000L)
        vm.indyPartyConnection.observe(this, Observer { updateIndyPartyConnection(it) })
    }

    private fun updateIndyPartyConnection(resource: Resource<IndyPartyConnection>?) {
        resource?.let {
            when (it.state) {
                ResourceState.LOADING -> {
                    Toast.makeText(this, "Waiting for Indy Party Connection", Toast.LENGTH_LONG)
                }
                ResourceState.SUCCESS -> {
                    Toast.makeText(this, "Connection is created", Toast.LENGTH_LONG)
                    sendProofRequestReceiveAndVerify(it.data)
                }
                ResourceState.ERROR -> Toast.makeText(this, "Connection Error: ${it.message}", Toast.LENGTH_LONG)
            }
        }
    }

    private fun sendProofRequestReceiveAndVerify(indyPartyConnection: IndyPartyConnection?) {
        indyPartyConnection?.let { vm.sendProofRequestReceiveAndVerify(it) }
        vm.verified.observe(this, Observer { updateVerified(it) })
    }

    private fun updateVerified(resource: Resource<Boolean>?) {
        resource?.let {
            when (it.state) {
                ResourceState.LOADING -> {
                    Toast.makeText(this, "Verifying proof request", Toast.LENGTH_LONG)
                }
                ResourceState.SUCCESS -> {
                    Toast.makeText(this, "Verified", Toast.LENGTH_LONG)
                }
                ResourceState.ERROR -> Toast.makeText(this, "Verifying Error: ${it.message}", Toast.LENGTH_LONG)
            }
        }
    }

}