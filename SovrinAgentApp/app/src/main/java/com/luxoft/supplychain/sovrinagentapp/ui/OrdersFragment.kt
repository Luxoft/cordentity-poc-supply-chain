package com.luxoft.supplychain.sovrinagentapp.ui


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.communcations.SovrinAgentService
import com.luxoft.supplychain.sovrinagentapp.data.Product
import com.luxoft.supplychain.sovrinagentapp.ui.model.OrdersAdapter
import io.realm.Realm
import org.koin.android.ext.android.inject
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class OrdersFragment : Fragment() {

    private val api: SovrinAgentService by inject()
    private var mAdapter: OrdersAdapter? = null
    private val realm: Realm = Realm.getDefaultInstance()

    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment, container, false)

        val recyclerView = view.findViewById(R.id.fragment_list_rv) as RecyclerView

        val linearLayoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        mAdapter = OrdersAdapter(Realm.getDefaultInstance())
        recyclerView.adapter = mAdapter

        mSwipeRefreshLayout = view.findViewById(R.id.swipe_container)
        mSwipeRefreshLayout.setOnRefreshListener { updateMyOrders() }

        updateMyOrders()

        return view
    }


    private fun updateMyOrders() {
        mSwipeRefreshLayout.isRefreshing = true
        api.getPackages().subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mSwipeRefreshLayout.isRefreshing = false
                    saveOrders(it)
                }, {
                    error ->
                    mSwipeRefreshLayout.isRefreshing = false
                    Log.e("", error.message)
                })
    }


    private fun saveOrders(offers: List<Product>) {
        if (offers.isNotEmpty()) {
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(offers)
            realm.commitTransaction()
        }
    }
}