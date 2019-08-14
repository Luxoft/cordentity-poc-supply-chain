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
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection
import com.luxoft.blockchainlab.hyperledger.indy.IndyUser
import com.luxoft.blockchainlab.hyperledger.indy.models.ProofRequest
import com.luxoft.blockchainlab.hyperledger.indy.utils.SerializationUtils
import com.luxoft.supplychain.sovrinagentapp.communcations.SovrinAgentService
import com.luxoft.supplychain.sovrinagentapp.ui.MainActivity.Companion.popupStatus
import com.luxoft.supplychain.sovrinagentapp.ui.MainActivity.Companion.showAlertDialog
import io.realm.Realm
import org.koin.android.ext.android.inject
import rx.Completable
import rx.schedulers.Schedulers
import java.lang.StringBuilder
import java.util.concurrent.atomic.AtomicInteger


class AskClaimsActivity : AppCompatActivity() {

    private val realm: Realm = Realm.getDefaultInstance()
    private val api: SovrinAgentService by inject()
    private val indyUser: IndyUser by inject()
    private val agentConnection: AgentConnection by inject()
    lateinit var proofRequest: ProofRequest
    var requestedDataBuilder = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_ask_claims)

        proofRequest = SerializationUtils.jSONToAny<ProofRequest>(intent?.getStringExtra("proofRequest")!!)
        val requestedData = proofRequest.requestedAttributes.keys + proofRequest.requestedPredicates.keys
        for (key in requestedData) {
            requestedDataBuilder.append(", $key")
        }
        showAlertDialogToProvide()
    }

    fun showAlertDialogToProvide() = AlertDialog.Builder(this)
            .setTitle("Claims request")
            .setMessage("Treatment center \"TC SEEHOF\" requesting your " + requestedDataBuilder.toString().substring(2) + " to approve your request.Provide it ?")
//            .setMessage("Treatment center \"TC SEEHOF\" requesting your Full Name, Date of Birth and Address to approve your request. Provide it?")
            .setCancelable(false)
            .setPositiveButton("PROVIDE", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    provide()
                }
            })
            .setNegativeButton("CANCEL", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    this@AskClaimsActivity.finish()
                }
            })
            .show()

    private fun provide() {
//        val recyclerView = findViewById<RecyclerView>(R.id.fragment_list_rv)
//        val linearLayoutManager = LinearLayoutManager(this)
//        recyclerView.layoutManager = linearLayoutManager
//        recyclerView.setHasFixedSize(true)
//        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, linearLayoutManager.orientation))
//        recyclerView.adapter = ClaimsAdapter(realm.where(ClaimAttribute::class.java).`in`("key", requestedData.toTypedArray()).findAll())
//        findViewById<Button>(R.id.accept_claims_request).setOnClickListener {
//        drawProgressBar()

        Completable.complete().observeOn(Schedulers.io()).subscribe({
            popupStatus = AtomicInteger(1)
            finish()

            val partyDid = intent?.getStringExtra("partyDID")!!
            val proofFromLedgerData = indyUser.createProofFromLedgerData(proofRequest)
            val connection = agentConnection.getIndyPartyConnection(partyDid).toBlocking().value()
                    ?: throw RuntimeException("Agent connection with $partyDid not found")

            connection.sendProof(proofFromLedgerData)

            // TODO: this api call should return immediately
            // TODO: after this you should listen to new ingoing credential offer
            // TODO: when offer appears, you should show a popup with something like "Treatment Center wants to issue you a new credential which will be used as a token for the package, agree?"
            // TODO: if agree you should send new credential request and listen to new credential
            // TODO: only when credential is sent Corda-side should commit transaction

            val credentialOffer = connection.receiveCredentialOffer().toBlocking().value()

            val credentialRequest = indyUser.createCredentialRequest(indyUser.walletUser.getIdentityDetails().did, credentialOffer)
            connection.sendCredentialRequest(credentialRequest)

            val credential = connection.receiveCredential().toBlocking().value()
            indyUser.checkLedgerAndReceiveCredential(credential, credentialRequest, credentialOffer)

            popupStatus = AtomicInteger(2)

//            finish()


//            api.getTails()
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(
//                            { tails ->
//                                val dir = File(tailsPath)
//                                if (!dir.exists())
//                                    dir.mkdirs()
//
//                                tails.forEach { name, content ->
//                                    val file = File("$tailsPath/$name")
//                                    if (file.exists())
//                                        file.delete()
//                                    file.createNewFile()
//                                    file.writeText(content)
//                                }
//                                println("TAILS WRITTEN")
//                                finish()
//                            },
//                            { er ->
//                                Log.e("Get Tails Error: ", er.message, er)
//                                showAlertDialog(baseContext, "Get Tails Error: ${er.message}") { finish() }
//                            }
//                    )
        },
                { er ->
                    Log.e("Get Request Error: ", er.message, er)
                    showAlertDialog(baseContext, "Get Request Error: ${er.message}") { finish() }
                })
//        }
    }

}
