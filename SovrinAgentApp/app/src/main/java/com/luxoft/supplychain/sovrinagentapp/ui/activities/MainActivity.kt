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
import android.support.v7.app.AppCompatActivity
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.data.PopupStatus
import com.luxoft.supplychain.sovrinagentapp.ui.adapters.ViewPagerAdapter
import com.luxoft.supplychain.sovrinagentapp.ui.fragments.ClaimsFragment
import com.luxoft.supplychain.sovrinagentapp.ui.fragments.HistoryFragment
import com.luxoft.supplychain.sovrinagentapp.ui.fragments.OrdersFragment
import com.luxoft.supplychain.sovrinagentapp.utils.showNotification
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : AppCompatActivity() {

    private val realm: Realm = Realm.getDefaultInstance()
    private lateinit var ordersFragment: OrdersFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")
        System.setProperty("jna.debug_load", "true")

        setContentView(R.layout.activity_main)
        setupToolbar()
        setupViewPager()
        setupCollapsingToolbar()

        startTimer()
    }

    private fun setupCollapsingToolbar() {
        collapse_toolbar.isTitleEnabled = true
    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFrag(ClaimsFragment())
        ordersFragment = OrdersFragment()
        adapter.addFrag(ordersFragment)
        adapter.addFrag(HistoryFragment())
        viewpager.adapter = adapter

        tabs.setupWithViewPager(viewpager)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(false)
            it.title = "Mark Rubinshtein"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.removeAllChangeListeners()
        realm.close()
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
                    ordersFragment.onResume()
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