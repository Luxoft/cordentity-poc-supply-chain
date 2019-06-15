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
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.luxoft.blockchainlab.hyperledger.indy.IndyUser
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.data.ClaimAttribute
import com.luxoft.supplychain.sovrinagentapp.di.updateCredentialsInRealm
import com.luxoft.supplychain.sovrinagentapp.ui.model.ClaimsAdapter
import io.realm.Realm
import org.koin.android.ext.android.inject


class ClaimsFragment : Fragment() {

    private var mAdapter: ClaimsAdapter? = null
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private val realm: Realm = Realm.getDefaultInstance()
    private val indyUser: IndyUser by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_claims, container, false)

        val recyclerView = view.findViewById(R.id.fragment_list_rv) as RecyclerView
//        val getClaimButton = view.findViewById(R.id.get_claims) as Button
//        getClaimButton.setOnClickListener {
//            ContextCompat.startActivity(getClaimButton.context,
//                    Intent().setClass(getClaimButton.context, SimpleScannerActivity::class.java)
//                            .putExtra("state", PackageState.GETPROOFS.name), null
//            )
//        }
//        getClaimButtonon.visibility = View.VISIBLE
        var claimsQty = 7
        val textViewProfileHeaderRight = view.findViewById(R.id.textViewProfileHeaderRight) as TextView
        textViewProfileHeaderRight.text = StringBuilder().append("$claimsQty ").append(getString(R.string.verified_claims))
        val linearLayoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, linearLayoutManager.orientation))

        mAdapter = ClaimsAdapter(realm.where(ClaimAttribute::class.java).sort("key").findAll())
        recyclerView.adapter = mAdapter

        mSwipeRefreshLayout = view.findViewById(R.id.swipe_container)
        mSwipeRefreshLayout.setOnRefreshListener { updateMyClaims() }

        if (ContextCompat.checkSelfPermission(this.requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //TODO: Breaks async UX
            updateMyClaims()
        }

        return view
    }

    private fun updateMyClaims() {
        mSwipeRefreshLayout.isRefreshing = true

        indyUser.walletUser.updateCredentialsInRealm()

        mSwipeRefreshLayout.isRefreshing = false
    }
}
