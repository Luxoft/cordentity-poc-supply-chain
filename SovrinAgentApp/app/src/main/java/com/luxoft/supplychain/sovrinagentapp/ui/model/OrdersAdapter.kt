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
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.data.PackageState
import com.luxoft.supplychain.sovrinagentapp.data.Product
import com.luxoft.supplychain.sovrinagentapp.ui.DigitalReceiptActivity
import com.luxoft.supplychain.sovrinagentapp.ui.SimpleScannerActivity
import com.luxoft.supplychain.sovrinagentapp.ui.TrackPackageActivity
import com.luxoft.supplychain.sovrinagentapp.utils.DateTimeUtils
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.item_history.view.*
import kotlinx.android.synthetic.main.item_order.view.*


class OrdersAdapter(realm: Realm) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var realmChangeListener = RealmChangeListener<Realm> {
        Log.i("TAG", "Change occurred!")
        this.notifyDataSetChanged()
    }

    init {
        realm.addChangeListener(realmChangeListener)
    }

    //Dirty hack to hide first hardcoded value if there are pending orders
    private val orders = object {
        private val orders: RealmResults<Product> = realm.where(Product::class.java).sort("requestedAt", Sort.DESCENDING).isNull("collectedAt").findAll()
        val size: Int
            get() {
                var size = orders.size
                if (size > 1)
                    size -= 1
                return size
            }

        operator fun get(index: Int): Product? {
            var position: Int = index
            if (orders.size > 1) {
                position += 1
            }
            return orders[position]
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
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
//        holder.sn.text = "SN: " + order.serial
        holder.message.text = order.currentStateMessage(PackageState.valueOf(order.state!!).ordinal)
        holder.title.setOnClickListener {
            startActivity(holder.title.context, Intent().setClass(holder.title.context, TrackPackageActivity::class.java).putExtra("serial", order.serial), null)
        }
        holder.qrButton.setOnClickListener {
            ContextCompat.startActivity(holder.qrButton.context,
                    Intent().setClass(holder.qrButton.context, SimpleScannerActivity::class.java)
                            .putExtra("serial", order.serial)
                            .putExtra("state", order.state), null
            )
        }
        holder.showReceiptButton.setOnClickListener {
            ContextCompat.startActivity(holder.showReceiptButton.context,
                    Intent().setClass(holder.showReceiptButton.context, DigitalReceiptActivity::class.java)
                            .putExtra("serial", order.serial)
                            .putExtra("state", order.state), null
            )
        }
        if (order.state.equals(PackageState.ISSUED.name) || order.state.equals(PackageState.PROCESSED.name)) {
           holder.qrButton.setVisibility(View.GONE)
           holder.showReceiptButton.setVisibility(View.VISIBLE)
            holder.textViewAddressHeader.setVisibility(View.GONE)
            holder.textViewAddress.setVisibility(View.GONE)
            holder.imageViewMapMarker.setVisibility(View.GONE)
        }
        if (order.state.equals(PackageState.DELIVERED.name)) {
            holder.qrButton.setVisibility(View.VISIBLE)
            holder.showReceiptButton.setVisibility(View.VISIBLE)
            holder.textViewAddressHeader.setVisibility(View.GONE)
            holder.textViewAddress.setVisibility(View.GONE)
            holder.imageViewMapMarker.setVisibility(View.GONE)
        }

        holder.textViewOrderItemDate.text = DateTimeUtils.parseDateTime(order.collectedAt!!, "dd MMM yyyy")
    }

    private fun bindNormalItem(order: Product, holder: OrderViewHolder) {
        holder.title.text = order.medicineName
//        holder.sn.text = "SN: " + order.serial
        holder.message.text = order.currentStateMessage(PackageState.valueOf(order.state!!).ordinal)
        holder.title.setOnClickListener {
            startActivity(holder.title.context, Intent().setClass(holder.title.context, TrackPackageActivity::class.java).putExtra("serial", order.serial), null)
        }
        holder.qrButton.setOnClickListener {
            ContextCompat.startActivity(holder.qrButton.context,
                    Intent().setClass(holder.qrButton.context, SimpleScannerActivity::class.java)
                            .putExtra("serial", order.serial)
                            .putExtra("state", order.state), null
            )
        }
        holder.showReceiptButton.setOnClickListener {
            ContextCompat.startActivity(holder.showReceiptButton.context,
                    Intent().setClass(holder.showReceiptButton.context, DigitalReceiptActivity::class.java)
                            .putExtra("serial", order.serial)
                            .putExtra("state", order.state), null
            )
        }
        if (order.state.equals(PackageState.ISSUED.name) || order.state.equals(PackageState.PROCESSED.name)) {
            holder.qrButton.setVisibility(View.GONE)
            holder.showReceiptButton.setVisibility(View.VISIBLE)
            holder.textViewAddressHeader.setVisibility(View.GONE)
            holder.textViewAddress.setVisibility(View.GONE)
            holder.imageViewMapMarker.setVisibility(View.GONE)

        }
        if (order.state.equals(PackageState.DELIVERED.name)) {
            holder.qrButton.setVisibility(View.VISIBLE)
            holder.showReceiptButton.setVisibility(View.VISIBLE)
            holder.textViewAddressHeader.setVisibility(View.GONE)
            holder.textViewAddress.setVisibility(View.GONE)
            holder.imageViewMapMarker.setVisibility(View.GONE)
        }

        holder.textViewOrderItemDate.text = DateTimeUtils.parseDateTime(order.collectedAt!!, "dd MMM yyyy")
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == QR) {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_order, viewGroup, false)
            QROrderViewHolder(view)
        } else {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_order, viewGroup, false)
            OrderViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return orders.size
    }

    override fun getItemViewType(position: Int): Int {
        val order = orders[position]
        return if (order?.state == PackageState.NEW.name || order?.state == PackageState.DELIVERED.name) QR else plain
    }

    open inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.textViewOrderItemMedicineName as TextView
        var message: TextView = itemView.textViewOrderItemMessage as TextView
        var textViewAddressHeader: TextView = itemView.textViewAddressHeader as TextView
        var textViewAddress: TextView = itemView.textViewAddress as TextView
        var textViewOrderItemDate: TextView = itemView.textViewOrderItemDate as TextView
        var imageViewMapMarker: ImageView = itemView.imageViewMapMarker as ImageView
        var showReceiptButton: View = itemView.linearLayoutShowReceipt
        var qrButton: View = itemView.linearLayoutScanQr
//        var sn: TextView = itemView.listitem_sn as TextView
    }

    open inner class QROrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.textViewOrderItemMedicineName as TextView
        var message: TextView = itemView.textViewOrderItemMessage as TextView
        var textViewAddressHeader: TextView = itemView.textViewAddressHeader as TextView
        var textViewAddress: TextView = itemView.textViewAddress as TextView
        var textViewOrderItemDate: TextView = itemView.textViewOrderItemDate as TextView
        var imageViewMapMarker: ImageView = itemView.imageViewMapMarker as ImageView
        var showReceiptButton: View = itemView.linearLayoutShowReceipt
        var qrButton: View = itemView.linearLayoutScanQr
//        var sn: TextView = itemView.qr_listitem_sn as TextView
    }


    companion object {
        const val QR = 0
        const val plain = 1
    }
}
