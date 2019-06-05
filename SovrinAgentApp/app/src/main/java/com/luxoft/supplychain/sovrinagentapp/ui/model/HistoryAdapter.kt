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

package com.luxoft.supplychain.sovrinagentapp.ui.model

import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.data.PackageState
import com.luxoft.supplychain.sovrinagentapp.data.Product
import com.luxoft.supplychain.sovrinagentapp.ui.TrackPackageActivity
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.order_list_item.view.*
import kotlinx.android.synthetic.main.qr_list_item.view.*


class HistoryAdapter(realm: Realm) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var realmChangeListener = RealmChangeListener<Realm> {
        Log.i("TAG", "Change occurred!")
        this.notifyDataSetChanged()
    }

    init {
        realm.addChangeListener(realmChangeListener)
    }

    private val orders: RealmResults<Product> = realm.where(Product::class.java).sort("collectedAt", Sort.DESCENDING).isNotNull("collectedAt").findAll()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val order = orders[position]

        if (order == null) {
            Log.i("TAG", "Item not found for index $position")
        } else when (holder) {
            is OrderViewHolder -> bindNormalItem(order, holder)
            is QROrderViewHolder -> bindQRItem(order, holder)
        }
    }

    private fun bindQRItem(order: Product, holder: QROrderViewHolder) {
        holder.title.text = order.medicineName
        holder.sn.text = "SN: " + order.serial
        holder.message.text = order.currentStateMessage(PackageState.valueOf(order.state!!).ordinal)
        holder.title.setOnClickListener {
            startActivity(holder.title.context, Intent().setClass(holder.title.context, TrackPackageActivity::class.java).putExtra("serial", order.serial), null)
        }
    }

    private fun bindNormalItem(order: Product, holder: OrderViewHolder) {
        holder.title.text = order.medicineName
        holder.sn.text = "SN: " + order.serial
        holder.message.text = order.currentStateMessage(PackageState.valueOf(order.state!!).ordinal)
        holder.title.setOnClickListener {
            startActivity(holder.title.context, Intent().setClass(holder.title.context, TrackPackageActivity::class.java).putExtra("serial", order.serial), null)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.order_list_item, viewGroup, false)
        return OrderViewHolder(view)
    }

    override fun getItemCount(): Int {
        return orders.size
    }

    override fun getItemViewType(position: Int): Int {
        return plain
    }

    open inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.listitem_medicine_name as TextView
        var message: TextView = itemView.listitem_message as TextView
        var sn: TextView = itemView.listitem_sn as TextView
    }

    open inner class QROrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.qr_listitem_medicine_name as TextView
        var message: TextView = itemView.qr_listitem_message as TextView
        var qrButton: View = itemView.btn_scan_qr
        var sn: TextView = itemView.qr_listitem_sn as TextView
    }


    companion object {
        const val QR = 0
        const val plain = 1
    }
}
