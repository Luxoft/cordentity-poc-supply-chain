package com.luxoft.supplychain.sovrinagentapp.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.luxoft.blockchainlab.hyperledger.indy.utils.SerializationUtils
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.application.AUTHORITIES
import com.luxoft.supplychain.sovrinagentapp.data.AuthorityInfoMap
import com.luxoft.supplychain.sovrinagentapp.data.ClaimAttribute
import com.luxoft.supplychain.sovrinagentapp.utils.inflate
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_digital_receipt.*
import kotlinx.android.synthetic.main.item_receipt.view.*

class DigitalReceiptActivity : AppCompatActivity() {
    private val realm: Realm = Realm.getDefaultInstance()
    private val claims: RealmResults<ClaimAttribute> = realm.where(ClaimAttribute::class.java).findAll()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_digital_receipt)

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_back)
        }

        claims.find { it.key.equals(AUTHORITIES) }
            ?.let { attr ->
                SerializationUtils.jSONToAny(attr.value!!, AuthorityInfoMap::class.java).run {
                    for (mutableEntry in this) {
                        val view = this@DigitalReceiptActivity.inflate(R.layout.item_receipt)
                        with(view) {
                            tvReceiptItemHeader.text = mutableEntry.key
                            if (mutableEntry.key.startsWith("T")) {
                                tvReceiptItemName.text = "TC SEEHOF"
                            } else {
                                tvReceiptItemName.text = "Manufacturing Astura 673434"
                            }
                            tvReceiptItemDID.text = getString(R.string.receipt_did, mutableEntry.value.did)
                            tvReceiptItemSchemaId.text = getString(R.string.receipt_schema_id, mutableEntry.value.schemaId)
                        }
                        receiptContainer.addView(view)
                    }
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}