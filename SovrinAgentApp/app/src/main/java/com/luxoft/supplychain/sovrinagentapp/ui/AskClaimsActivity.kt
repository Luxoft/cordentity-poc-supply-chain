package com.luxoft.supplychain.sovrinagentapp.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Button
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.communcations.SovrinAgentService
import com.luxoft.supplychain.sovrinagentapp.data.AskForPackageRequest
import com.luxoft.supplychain.sovrinagentapp.data.ClaimAttribute
import com.luxoft.supplychain.sovrinagentapp.data.Product
import com.luxoft.supplychain.sovrinagentapp.ui.model.ClaimsAdapter
import io.realm.Realm
import org.koin.android.ext.android.inject
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class AskClaimsActivity : AppCompatActivity() {

    private val realm: Realm = Realm.getDefaultInstance()
    private val api: SovrinAgentService by inject()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask_claims)

        val recyclerView = findViewById<RecyclerView>(R.id.fragment_list_rv)

        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        recyclerView.addItemDecoration( DividerItemDecoration(recyclerView.context, linearLayoutManager.orientation))

        recyclerView.adapter = ClaimsAdapter(Realm.getDefaultInstance().where(ClaimAttribute::class.java).findAll())

        findViewById<Button>(R.id.accept_claims_request).setOnClickListener {
            ContextCompat.startActivity(this, Intent().setClass(this,
                    MainActivity::class.java),
                    null)

            api.createRequest(AskForPackageRequest("TreatmentCenter")).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        realm.beginTransaction()
                        realm.where(Product::class.java).equalTo("serial", "N/A").findAll().deleteAllFromRealm()
                        realm.commitTransaction()
                        finish()
                    }, {
                        error ->
                        Log.e("", error.message)
                        finish()
                    })


        }
    }

}
