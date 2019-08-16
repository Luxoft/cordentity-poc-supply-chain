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
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.data.PackageState
import com.luxoft.supplychain.sovrinagentapp.data.Product
import com.luxoft.supplychain.sovrinagentapp.ui.*
import com.luxoft.supplychain.sovrinagentapp.utils.DateTimeUtils
import com.luxoft.supplychain.sovrinagentapp.utils.inflate
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort
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

    //region ******************** OVERRIDE *********************************************************

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            QR -> QROrderViewHolder(viewGroup.context.inflate(R.layout.item_order, viewGroup))
            else -> OrderViewHolder(viewGroup.context.inflate(R.layout.item_order, viewGroup))
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

    override fun getItemCount(): Int {
        return orders.size
    }

    override fun getItemViewType(position: Int): Int {
        val order = orders[position]
        return if (order?.state == PackageState.NEW.name || order?.state == PackageState.DELIVERED.name) QR else PLAIN
    }

    //endregion OVERRIDE

    private fun bindQRItem(order: Product, holder: QROrderViewHolder) {

    }

    private fun bindNormalItem(order: Product, holder: OrderViewHolder) {

    }

    //region ******************** HOLDERS **********************************************************

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

        fun bind(order: Product?) {
            order?.let {
                title.text = order.medicineName
//                sn.text = "SN: " + order.serial
                message.text = order.currentStateMessage(PackageState.valueOf(order.state!!).ordinal)
                title.setOnClickListener {
                    startActivity(title.context, Intent().setClass(title.context, TrackPackageActivity::class.java).putExtra("serial", order.serial), null)
                }
                qrButton.setOnClickListener {
                    ContextCompat.startActivity(qrButton.context,
                        Intent().setClass(qrButton.context, SimpleScannerActivity::class.java)
                            .putExtra("serial", order.serial)
                            .putExtra("state", order.state), null
                    )
                }
                showReceiptButton.setOnClickListener {
                    ContextCompat.startActivity(showReceiptButton.context,
                        Intent().setClass(showReceiptButton.context, DigitalReceiptActivity::class.java)
                            .putExtra("serial", order.serial)
                            .putExtra("state", order.state), null
                    )
                }
                if (order.state.equals(PackageState.ISSUED.name) || order.state.equals(PackageState.PROCESSED.name)) {
                    qrButton.setVisibility(View.GONE)
                    showReceiptButton.setVisibility(View.VISIBLE)
                    textViewAddressHeader.setVisibility(View.GONE)
                    textViewAddress.setVisibility(View.GONE)
                    imageViewMapMarker.setVisibility(View.GONE)
                }
                if (order.state.equals(PackageState.DELIVERED.name)) {
                    qrButton.setVisibility(View.VISIBLE)
                    showReceiptButton.setVisibility(View.VISIBLE)
                    textViewAddressHeader.setVisibility(View.GONE)
                    textViewAddress.setVisibility(View.GONE)
                    imageViewMapMarker.setVisibility(View.GONE)
                }
                textViewOrderItemDate.text =  DateTimeUtils.parseDateTime (order?.deliveredAt
                    ?: order?.processedAt ?: order?.issuedAt
                    ?: System.currentTimeMillis(), "dd MMM yyyy")
            }
        }
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

    //endregion HOLDERS

    companion object {
        const val QR = 0
        const val PLAIN = 1
    }
}
