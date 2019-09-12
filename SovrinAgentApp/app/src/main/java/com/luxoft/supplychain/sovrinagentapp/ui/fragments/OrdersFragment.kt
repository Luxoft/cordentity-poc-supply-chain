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

package com.luxoft.supplychain.sovrinagentapp.ui.fragments

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
import com.luxoft.supplychain.sovrinagentapp.data.PackageState
import com.luxoft.supplychain.sovrinagentapp.data.Product
import com.luxoft.supplychain.sovrinagentapp.data.ProductOperation
import com.luxoft.supplychain.sovrinagentapp.ui.activities.MainActivity.Companion.showAlertDialog
import com.luxoft.supplychain.sovrinagentapp.ui.adapters.OrdersAdapter
import com.luxoft.supplychain.sovrinagentapp.utils.showNotification
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.fragment_recycler.*
import org.koin.android.ext.android.inject
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class OrdersFragment : Fragment() {

    private val api: SovrinAgentService by inject()
    private val realm: Realm = Realm.getDefaultInstance()
    private var recyclerAdapter: OrdersAdapter? = null

    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    private val productOperations: RealmResults<ProductOperation> = realm.where(ProductOperation::class.java).findAll()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recycler, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(recycler) {
            layoutManager = LinearLayoutManager(activity)
            recyclerAdapter = OrdersAdapter(Realm.getDefaultInstance())
            adapter = recyclerAdapter
        }
        mSwipeRefreshLayout = swipe_container
        mSwipeRefreshLayout.setOnRefreshListener { updateMyOrders() }
    }

    override fun onResume() {
        super.onResume()
        updateMyOrders()
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
        mSwipeRefreshLayout.isRefreshing = false
    }

    private fun saveOrders(offers: List<Product>) {
        if (offers.isNotEmpty()) {
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(offers)
            realm.commitTransaction()
        }
        for (offer in offers) {
            if (offer.state.equals(PackageState.DELIVERED.name)) {
                var delivered = false
                for (productOperation in productOperations) {
                    if (offer.deliveredAt!! == productOperation.at
                        && productOperation.by.equals("delivered")) delivered = true
                }
                if (!delivered) {
                    showNotification(activity!!, getString(R.string.your_package_is_ready), getString(R.string.visit_your))
                    Realm.getDefaultInstance().executeTransaction {
                        val productOperation = it.createObject(ProductOperation::class.java, offer.deliveredAt)
                        productOperation.by = "delivered"
                    }
                }
            }
        }
    }
}
