package com.luxoft.supplychain.sovrinagentapp.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection
import com.luxoft.blockchainlab.hyperledger.indy.IndyUser
import com.luxoft.blockchainlab.hyperledger.indy.models.CredentialValue
import com.luxoft.blockchainlab.hyperledger.indy.models.ProofRequest
import com.luxoft.blockchainlab.hyperledger.indy.utils.SerializationUtils
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.data.AuthorityInfoMap
import com.luxoft.supplychain.sovrinagentapp.data.ClaimAttribute
import com.luxoft.supplychain.sovrinagentapp.data.Product
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import org.koin.android.ext.android.inject
import rx.Completable
import rx.schedulers.Schedulers

class DigitalReceiptActivity : AppCompatActivity() {
    private val indyUser: IndyUser by inject()
    private val agentConnection: AgentConnection by inject()
    private val realm: Realm = Realm.getDefaultInstance()

    private val claims: RealmResults<ClaimAttribute> = realm.where(ClaimAttribute::class.java).findAll()
    lateinit var authorityInfoMap: AuthorityInfoMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_digital_receipt)

        title = "Digital Receipt"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)

        for (claim in claims) {
            if (claim.key.equals("authorities")) {
                authorityInfoMap = SerializationUtils.jSONToAny(claim.value!!, AuthorityInfoMap::class.java)
            }
        }
        val linearLayoutReceiptContent = findViewById(R.id.linearLayoutReceiptContent) as LinearLayout
        var textViewReceiptItemHeader: TextView
        var textViewReceiptItemName: TextView
        var textViewReceiptItemDID: TextView
        var textViewReceiptItemSchemaId: TextView

        for (mutableEntry in authorityInfoMap) {

            val view: View? = layoutInflater.inflate(R.layout.item_receipt, null)
            textViewReceiptItemHeader = view?.findViewById(R.id.textViewReceiptItemHeader) as TextView
            textViewReceiptItemName = view?.findViewById(R.id.textViewReceiptItemName) as TextView
            textViewReceiptItemDID = view?.findViewById(R.id.textViewReceiptItemDID) as TextView
            textViewReceiptItemSchemaId = view?.findViewById(R.id.textViewReceiptItemSchemaId) as TextView

            textViewReceiptItemHeader.text = mutableEntry.key
            if (mutableEntry.key.startsWith("T")) {
                textViewReceiptItemName.text = "TC SEEHOF"
            } else {
                textViewReceiptItemName.text = "Manufacturing Astura 673434"
            }
            textViewReceiptItemDID.text = mutableEntry.value.did
            textViewReceiptItemSchemaId.text = mutableEntry.value.schemaId


            linearLayoutReceiptContent.addView(view)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}