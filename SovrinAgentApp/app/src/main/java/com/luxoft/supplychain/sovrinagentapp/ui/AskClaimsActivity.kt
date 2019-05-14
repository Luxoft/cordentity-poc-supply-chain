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
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Button
import com.luxoft.blockchainlab.hyperledger.indy.IndyUser
import com.luxoft.blockchainlab.hyperledger.indy.helpers.TailsHelper
import com.luxoft.blockchainlab.hyperledger.indy.models.CredentialDefinitionId
import com.luxoft.blockchainlab.hyperledger.indy.models.RevocationRegistryInfo
import com.luxoft.supplychain.sovrinagentapp.Application
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.communcations.SovrinAgentService
import com.luxoft.supplychain.sovrinagentapp.data.AskForPackageRequest
import com.luxoft.supplychain.sovrinagentapp.data.ClaimAttribute
import com.luxoft.supplychain.sovrinagentapp.ui.MainActivity.Companion.showAlertDialog
import com.luxoft.supplychain.sovrinagentapp.ui.model.ClaimsAdapter
import io.realm.Realm
import org.koin.android.ext.android.inject
import rx.Completable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class AskClaimsActivity : AppCompatActivity() {

    private val realm: Realm = Realm.getDefaultInstance()
    private val api: SovrinAgentService by inject()
    private val indyUser: IndyUser by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask_claims)

        val recyclerView = findViewById<RecyclerView>(R.id.fragment_list_rv)

        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, linearLayoutManager.orientation))

        recyclerView.adapter = ClaimsAdapter(realm.where(ClaimAttribute::class.java).findAll())

        findViewById<Button>(R.id.accept_claims_request).setOnClickListener {
            drawProgressBar()
            //Hack for Retrofit blocking UI thread
            Completable.complete().observeOn(Schedulers.io()).subscribe {
                api.createRequest(AskForPackageRequest(indyUser.did))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({

                        // TODO: this api call should return immediately
                        // TODO: after this you should listen to new ingoing credential offer
                        // TODO: when offer appears, you should show a popup with something like "Treatment Center wants to issue you a new credential which will be used as a token for the package, agree?"
                        // TODO: if agree you should send new credential request and listen to new credential
                        // TODO: only when credential is sent Corda-side should commit transaction

                        val connection = (application as Application).getConnection()

                        val credentialOffer = connection.receiveCredentialOffer().toBlocking().value()

                        val credentialRequest = indyUser.createCredentialRequest(indyUser.did, credentialOffer)
                        connection.sendCredentialRequest(credentialRequest)

                        val credential = connection.receiveCredential().toBlocking().value()
                        val revocationRegistryInfo : RevocationRegistryInfo? =
                            indyUser.getRevocationRegistryInfo(
                                CredentialDefinitionId.fromString(
                                    credential.credential.credentialDefinitionIdRaw
                                ))
                        val tailsHash = revocationRegistryInfo!!.definition.value["tailsHash"].toString()
                        indyUser.receiveCredential(credential, credentialRequest, credentialOffer)
                        connection.requestTails(tailsHash)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                TailsHelper.DefaultWriter(indyUser.tailsPath).write(it)
                                println("TAILS WRITTEN")
                                finish()
                            }

                    }, {
                        er -> Log.e("Get Request Error: ", er.message, er)
                        showAlertDialog(baseContext, "Get Request Error: ${er.message}") { finish() }
                    })
            }
        }
    }

}
