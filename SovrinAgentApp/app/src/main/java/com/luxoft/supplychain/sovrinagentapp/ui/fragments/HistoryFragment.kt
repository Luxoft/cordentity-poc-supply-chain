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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.data.ApplicationState
import com.luxoft.supplychain.sovrinagentapp.data.Product
import com.luxoft.supplychain.sovrinagentapp.ui.adapters.HistoryAdapter
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_recycler.*
import org.koin.android.ext.android.inject

class HistoryFragment : Fragment() {

    private val appState: ApplicationState by inject()

    private val realm: Realm = Realm.getDefaultInstance()
    private lateinit var recyclerAdapter: HistoryAdapter

    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recycler, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(recycler) {
            layoutManager = LinearLayoutManager(activity)
            recyclerAdapter = HistoryAdapter(Realm.getDefaultInstance(), this@HistoryFragment)
            adapter = recyclerAdapter
        }

        mSwipeRefreshLayout = swipe_container
        mSwipeRefreshLayout.setOnRefreshListener {
            updateMyOrders()
            mSwipeRefreshLayout.isRefreshing = false
        }

        updateMyOrders()
    }

    private fun updateMyOrders() {
        // getPackages() -> saveOrders()
    }

    private fun saveOrders(offers: List<Product>) {
        if (offers.isNotEmpty()) {
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(offers)
            realm.commitTransaction()
        }
    }
}
