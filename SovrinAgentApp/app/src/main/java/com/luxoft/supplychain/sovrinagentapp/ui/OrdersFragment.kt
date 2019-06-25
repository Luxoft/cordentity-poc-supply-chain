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


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.communcations.SovrinAgentService
import com.luxoft.supplychain.sovrinagentapp.data.Product
import com.luxoft.supplychain.sovrinagentapp.ui.MainActivity.Companion.showAlertDialog
import com.luxoft.supplychain.sovrinagentapp.ui.model.OrdersAdapter
import io.realm.Realm
import org.koin.android.ext.android.inject
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicInteger
import android.view.Gravity
import android.view.WindowManager
import android.app.Dialog
import android.content.Context
import android.widget.TextView
import com.luxoft.supplychain.sovrinagentapp.data.PackageState
import com.luxoft.supplychain.sovrinagentapp.data.ProductOperation
import io.realm.RealmResults
import rx.Observable
import java.util.concurrent.TimeUnit


class OrdersFragment : Fragment() {

    private val api: SovrinAgentService by inject()
    private val agentConnection: AgentConnection by inject()
    private var mAdapter: OrdersAdapter? = null
    private val realm: Realm = Realm.getDefaultInstance()

    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private val loaded = AtomicInteger(0)
    lateinit var dialog: Dialog

    private val productOperations: RealmResults<ProductOperation> = realm.where(ProductOperation::class.java).findAll()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_orders, container, false)

        val recyclerView = view.findViewById(R.id.fragment_list_rv) as RecyclerView

        val linearLayoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        mAdapter = OrdersAdapter(Realm.getDefaultInstance())
        recyclerView.adapter = mAdapter

        mSwipeRefreshLayout = view.findViewById(R.id.swipe_container)
        mSwipeRefreshLayout.setOnRefreshListener {
            loaded.compareAndSet(0, 1)
            updateMyOrders()
        }
        loaded.set(1)
        updateMyOrders()

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        updateMyOrders2()
    }

    override fun onResume() {
        super.onResume()
        loaded.compareAndSet(0, 1)
        updateMyOrders()
    }

    private fun updateMyOrders2() {
        api.getPackages().subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    loaded()
                    saveOrders(it)
                }, { error ->
                    Log.e("Get Packages Error: ", error.message, error)
//                    showAlertDialog(context!!, "Get Packages Error: ${error.message}") { loaded() }
                })
    }

    private fun updateMyOrders() {
        mSwipeRefreshLayout.isRefreshing = true
        api.getPackages().subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    loaded()
                    saveOrders(it)
                }, { error ->
                    Log.e("Get Packages Error: ", error.message, error)
                    showAlertDialog(context!!, "Get Packages Error: ${error.message}") { loaded() }
                })
    }

    private fun loaded() {
        if (loaded.decrementAndGet() <= 0) {
            mSwipeRefreshLayout.isRefreshing = false
        }
    }

    private fun saveOrders(offers: List<Product>) {
        if (offers.isNotEmpty()) {
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(offers)
            realm.commitTransaction()
        }
        for (offer in offers) {
//            if (offer.state.equals(PackageState.ISSUED.name)) {
//                var issued: Boolean = false
//                for (productOperation in productOperations) {
//                    if (offer.requestedAt!!.equals(productOperation.at) && productOperation.by.equals("requested")) issued = true
//                }
//                if (!issued) {
//                    showPopup(getString(R.string.new_digital_receipt), getString(R.string.you_ve_received))
//                    Realm.getDefaultInstance().executeTransaction {
//                        val productOperation = it.createObject(ProductOperation::class.java, offer.requestedAt)
//                        productOperation.by = "requested"
//                    }
//                }
//            }
            if (offer.state.equals(PackageState.DELIVERED.name)) {
                var delivered: Boolean = false
                for (productOperation in productOperations) {
                    if (offer.deliveredAt!!.equals(productOperation.at) && productOperation.by.equals("delivered")) delivered = true
                }
                if (!delivered) {
                    showPopup(getString(R.string.your_package_is_ready), getString(R.string.visit_your))
                    Realm.getDefaultInstance().executeTransaction {
                        val productOperation = it.createObject(ProductOperation::class.java, offer.deliveredAt)
                        productOperation.by = "delivered"
                    }
                }
            }
        }
    }

    fun showPopup(header: String, message: String) {
        dialog = Dialog(activity)
        dialog.setContentView(R.layout.popup_layout)
        val textViewPopupHeader: TextView = dialog.findViewById(R.id.textViewPopupHeader)
        val textViewPopupMessage: TextView = dialog.findViewById(R.id.textViewPopupMessage)
        textViewPopupHeader.text = header
        textViewPopupMessage.text = message
        val window = dialog.getWindow()
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.TOP)
        dialog.show()
        Observable.timer(10, TimeUnit.SECONDS).subscribe { aLong -> dialog.dismiss() }
    }

}
