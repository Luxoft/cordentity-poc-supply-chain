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

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.communcations.SovrinAgentService
import com.luxoft.supplychain.sovrinagentapp.data.Product
import com.luxoft.supplychain.sovrinagentapp.ui.MainActivity.Companion.showAlertDialog
import com.luxoft.supplychain.sovrinagentapp.ui.model.HistoryAdapter
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_recycler.*
import org.koin.android.ext.android.inject
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class HistoryFragment : Fragment() {

    private val api: SovrinAgentService by inject()
    private val realm: Realm = Realm.getDefaultInstance()
    private lateinit var recyclerAdapter: HistoryAdapter

    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recycler, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(recycler) {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            recyclerAdapter = HistoryAdapter(Realm.getDefaultInstance())
            adapter = recyclerAdapter
        }

        mSwipeRefreshLayout = swipe_container
        mSwipeRefreshLayout.setOnRefreshListener { updateMyOrders() }

        updateMyOrders()
    }

    private fun updateMyOrders() {
        mSwipeRefreshLayout.isRefreshing = true
        api.getPackages().subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mSwipeRefreshLayout.isRefreshing = false
                saveOrders(it)
            }, { error ->
                mSwipeRefreshLayout.isRefreshing = false
                showAlertDialog(context!!, "Error loading history: ${error.message}")
                Log.e("Error loading history: ", error.message, error)
            })
    }

    private fun saveOrders(offers: List<Product>) {
        if (offers.isNotEmpty()) {
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(offers)
            realm.commitTransaction()
        }
    }

    fun showAlertDialogToProvide() = AlertDialog.Builder(context)
        .setTitle("Claims request")
        .setMessage("Treatment center \"TC SEEHOF\" requesting your Full Name, Date of Birth and Address to approve your request. Provide it?")
        .setCancelable(false)
        .setPositiveButton("PROVIDE", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
            }
        })
        .setNegativeButton("CANCEL", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
            }
        })
        .show()
}
