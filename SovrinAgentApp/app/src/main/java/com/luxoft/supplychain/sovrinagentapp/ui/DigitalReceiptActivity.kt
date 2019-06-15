package com.luxoft.supplychain.sovrinagentapp.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.data.Product
import io.realm.Realm

class DigitalReceiptActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_digital_receipt)

        title = "Digital Receipt"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)

        val product = Realm.getDefaultInstance().where(Product::class.java).equalTo("serial", intent.getStringExtra("serial")).findFirst()
        if (product != null) {

            val linearLayoutReceiptContent = findViewById(R.id.linearLayoutReceiptContent) as LinearLayout

            for (i in 1..2) {
                val view: View? = layoutInflater.inflate(R.layout.item_receipt, null)
                val textViewReceiptItemHeader = view?.findViewById(R.id.textViewReceiptItemHeader) as TextView
                val textViewReceiptItemName = view?.findViewById(R.id.textViewReceiptItemName) as TextView
                val textViewReceiptItemDID = view?.findViewById(R.id.textViewReceiptItemDID) as TextView
                val textViewReceiptItemSchemaId = view?.findViewById(R.id.textViewReceiptItemSchemaId) as TextView

                linearLayoutReceiptContent.addView(view)
            }

        } else {
            Toast.makeText(this, "Item not found", Toast.LENGTH_LONG).show()
            finish()
        }
    }

}