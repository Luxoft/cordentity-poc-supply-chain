package com.luxoft.supplychain.sovrinagentapp.ui


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.ui.model.ClaimsAdapter
import io.realm.Realm
import android.support.v7.widget.DividerItemDecoration
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.luxoft.supplychain.sovrinagentapp.communcations.SovrinAgentService
import com.luxoft.supplychain.sovrinagentapp.data.ClaimAttribute
import org.koin.android.ext.android.inject
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class ClaimsFragment : Fragment() {

    private var mAdapter: ClaimsAdapter? = null
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private val api: SovrinAgentService by inject()
    private val realm: Realm = Realm.getDefaultInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment, container, false)

        val recyclerView = view.findViewById(R.id.fragment_list_rv) as RecyclerView

        val linearLayoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        recyclerView.addItemDecoration( DividerItemDecoration(recyclerView.context, linearLayoutManager.orientation))

        mAdapter = ClaimsAdapter(Realm.getDefaultInstance().where(ClaimAttribute::class.java).findAll())
        recyclerView.adapter = mAdapter

        mSwipeRefreshLayout = view.findViewById(R.id.swipe_container)
        mSwipeRefreshLayout.setOnRefreshListener { updateMyClaims() }

        updateMyClaims()

        return view
    }

    private fun updateMyClaims() {
        mSwipeRefreshLayout.isRefreshing = true
        api.getClaims().subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ claims ->
                    mSwipeRefreshLayout.isRefreshing = false

                    val claimJson = Gson().fromJson<JsonObject>(claims[0], JsonObject::class.java)

                    val claimAttrs = claimJson.get("values").asJsonObject.entrySet().map {
                            val claim = ClaimAttribute()
                            claim.key = it.key
                            claim.value = it.value.asJsonArray[0].asString
                            claim.issuer = claimJson.get("schema_key").asJsonObject.get("did").asString
                            claim
                    }

                    if (claimAttrs.isNotEmpty()) {
                        realm.beginTransaction()
                        realm.copyToRealmOrUpdate(claimAttrs)
                        realm.commitTransaction()
                    }
                }, {
                    error ->
                    mSwipeRefreshLayout.isRefreshing = false
                    Log.e("", error.message)
                })
    }
}