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

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.luxoft.blockchainlab.hyperledger.indy.IndyUser
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.data.ApplicationState
import com.luxoft.supplychain.sovrinagentapp.data.PopupStatus
import com.luxoft.supplychain.sovrinagentapp.ui.adapters.ViewPagerAdapter
import com.luxoft.supplychain.sovrinagentapp.ui.fragments.ProfileFragment
import com.luxoft.supplychain.sovrinagentapp.ui.fragments.VerificationsFragment
import com.luxoft.supplychain.sovrinagentapp.utils.showNotification
import com.luxoft.supplychain.sovrinagentapp.utils.updateCredentialsInRealm
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : AppCompatActivity() {

    private val realm: Realm = Realm.getDefaultInstance()

    private val appState: ApplicationState by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")
        System.setProperty("jna.debug_load", "true")

        appState.indyState.indyUser.observeForever { user: IndyUser ->
            user.walletUser.updateCredentialsInRealm()
        }

        setContentView(R.layout.activity_main)
        setupToolbar()
        setupViewPager()

        collapse_toolbar.isTitleEnabled = true

        startTimer()
    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(supportFragmentManager)

        adapter.addFrag(ProfileFragment())
        adapter.addFrag(VerificationsFragment())

        viewpager.adapter = adapter

        tabs.setupWithViewPager(viewpager)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        appState.user.observe({lifecycle}) { user ->
            supportActionBar?.title = user.name
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.removeAllChangeListeners()
        realm.close()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_claims, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.clear_wallet -> {
                appState.indyState.resetWallet()
                true
            }
            R.id.rescan_wallet -> {
                appState.updateWalletCredentials()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

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

    var uiHandler: Handler? = null
    lateinit var myTimer: Timer
    val timeoutRunnable = Runnable {
        when (popupStatus.get()) {
            PopupStatus.IN_PROGRESS.ordinal -> {
                if (!inProgress) {
                    showNotification(this, "In progress", "")
                    inProgress = true
                }
            }
            PopupStatus.RECEIVED.ordinal -> {
                if (inProgress) {
                    inProgress = false
                    showNotification(this, getString(R.string.new_digital_receipt), getString(R.string.you_ve_received))
                }
            }
            PopupStatus.HISTORY.ordinal -> {
                if (inProgress) {
                    inProgress = false
                    showNotification(this, "Package history", "Your package history is available")
                }
            }
        }
    }
    private val timeoutTimerTask = object : TimerTask() {
        override fun run() {
            uiHandler!!.post(timeoutRunnable)
        }
    }

    private fun startTimer() {
        myTimer = Timer()
        uiHandler = Handler()
        myTimer.schedule(timeoutTimerTask, 1L * 1000, 1L * 1000)
    }
}