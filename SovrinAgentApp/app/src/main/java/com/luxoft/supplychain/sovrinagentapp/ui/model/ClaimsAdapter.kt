package com.luxoft.supplychain.sovrinagentapp.ui.model

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.data.ClaimAttribute
import io.realm.RealmChangeListener
import io.realm.RealmResults
import kotlinx.android.synthetic.main.claim_list_item.view.*


class ClaimsAdapter(private val claims: RealmResults<ClaimAttribute>) : RecyclerView.Adapter<ClaimsAdapter.ClaimViewHolder>() {


   var realmChangeListener = RealmChangeListener<RealmResults<ClaimAttribute>> {
        Log.i("TAG", "Change occurred!")
        this.notifyDataSetChanged()
    }

    init {
        claims.addChangeListener(realmChangeListener)
    }


    override fun onBindViewHolder(holder: ClaimViewHolder, position: Int) {
        holder.name.text = claims[position]?.key
        holder.value.text = claims[position]?.value
        holder.issuer.text = claims[position]?.issuer
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ClaimViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.claim_list_item, viewGroup, false)
        return ClaimViewHolder(view)
    }

    override fun getItemCount(): Int {
        return claims.size
    }

    inner class ClaimViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var name: TextView = itemView.tv_claim_attr_name as TextView
        var value: TextView = itemView.tv_claim_attr_value as TextView
        var issuer: TextView = itemView.tv_claim_issuer as TextView

    }
}