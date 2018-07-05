package com.luxoft.supplychain.sovrinagentapp.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.widget.Toast
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.data.Product
import com.luxoft.supplychain.sovrinagentapp.ui.timeline.TimeLineAdapter
import io.realm.Realm


class TrackPackageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_product)

        val product = Realm.getDefaultInstance().where(Product::class.java).equalTo("serial", intent.getStringExtra("serial")).findFirst()

        if(product != null) {
            val timeline = findViewById<RecyclerView>(R.id.recyclerView)
            timeline.adapter = TimeLineAdapter(product)
            timeline.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            timeline.setHasFixedSize(true)

            title = product.medicineName
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        } else {
            Toast.makeText(this, "Item not found", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Kids, never try it at home
        onBackPressed()
        return true
    }
}
