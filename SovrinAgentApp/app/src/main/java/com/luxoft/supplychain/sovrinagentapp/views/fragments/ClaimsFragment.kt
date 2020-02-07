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

package com.luxoft.supplychain.sovrinagentapp.views.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.luxoft.blockchainlab.hyperledger.indy.IndyUser
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.application.AUTHORITIES
import com.luxoft.supplychain.sovrinagentapp.application.EXTRA_SERIAL
import com.luxoft.supplychain.sovrinagentapp.application.FIELD_KEY
import com.luxoft.supplychain.sovrinagentapp.application.TIME
import com.luxoft.supplychain.sovrinagentapp.data.ClaimAttribute
import com.luxoft.supplychain.sovrinagentapp.views.adapters.ClaimsAdapter
import com.luxoft.supplychain.sovrinagentapp.utils.updateCredentialsInRealm
import io.realm.Realm
import org.koin.android.ext.android.inject

class ClaimsFragment : Fragment() {

    private lateinit var adapterRecycler: ClaimsAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val realm: Realm = Realm.getDefaultInstance()
    private val indyUser: IndyUser by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_claims, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val claims = realm.where(ClaimAttribute::class.java)
            .sort(FIELD_KEY)
            .notEqualTo(FIELD_KEY, AUTHORITIES)
            .notEqualTo(FIELD_KEY, TIME)
            .notEqualTo(FIELD_KEY, EXTRA_SERIAL)
            .findAll()

        claims.addChangeListener { result ->
            val numCredRefs = result.distinctBy { it.credRefSeqNo }.size
            tvClaims.text = getString(R.string.verified_credentials, numCredRefs)
        }

        val linearLayoutManager = LinearLayoutManager(activity)
        recycler.layoutManager = linearLayoutManager
        recycler.addItemDecoration(DividerItemDecoration(recycler.context, linearLayoutManager.orientation))

        adapterRecycler = ClaimsAdapter(claims)
        recycler.adapter = adapterRecycler

        swipeRefreshLayout = swipe_container
        swipeRefreshLayout.setOnRefreshListener { updateMyClaims() }

        updateMyClaims()
    }

    private fun updateMyClaims() {
        swipeRefreshLayout.isRefreshing = true
        indyUser.walletUser.updateCredentialsInRealm()
        swipeRefreshLayout.isRefreshing = false
    }
}
