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

package com.luxoft.supplychain.sovrinagentapp.ui.adapters

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.application.*
import com.luxoft.supplychain.sovrinagentapp.data.PackageState
import com.luxoft.supplychain.sovrinagentapp.data.Product
import com.luxoft.supplychain.sovrinagentapp.ui.activities.DigitalReceiptActivity
import com.luxoft.supplychain.sovrinagentapp.ui.activities.SimpleScannerActivity
import com.luxoft.supplychain.sovrinagentapp.ui.activities.TrackPackageActivity
import com.luxoft.supplychain.sovrinagentapp.utils.*
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.block_medicine_info.view.*
import kotlinx.android.synthetic.main.item_order.view.*
import java.text.SimpleDateFormat
import java.util.*

class OrdersAdapter(realm: Realm) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    var realmChangeListener = RealmChangeListener<Realm> {
        Log.i("TAG", "Change occurred!")
        this.notifyDataSetChanged()
    }

    private val dateFormatter = createFormatter("dd MMM yyyy")

    private val qrCodeClickListener = object : OrderClickListener{
        override fun click(order: Product, context: Context) {
            ContextCompat.startActivity(context,
                Intent().setClass(context, SimpleScannerActivity::class.java)
                    .putExtra(EXTRA_SERIAL, order.serial)
                    .putExtra(EXTRA_STATE, order.state), null
            )
        }
    }

    private val receiptClickListner = object : OrderClickListener {
        override fun click(order: Product, context: Context) {
            ContextCompat.startActivity(context,
                Intent().setClass(context, DigitalReceiptActivity::class.java)
                    .putExtra(EXTRA_SERIAL, order.serial)
                    .putExtra(EXTRA_STATE, order.state), null
            )
        }
    }

    private val itemClickListener = object : OrderClickListener{
        override fun click(order: Product, context: Context) {
            startActivity(context, Intent().setClass(context, TrackPackageActivity::class.java).putExtra(EXTRA_SERIAL, order.serial), null)
        }
    }

    init {
        realm.addChangeListener(realmChangeListener)
    }

    //Dirty hack to hide first hardcoded value if there are pending orders
    private val orders = object {
        private val orders: RealmResults<Product> = realm.where(Product::class.java)
            .sort(FIELD_REQUESTED_AT, Sort.DESCENDING)
            .isNull(FIELD_COLLECTED_AT)
            .findAll()
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

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(viewGroup.context.inflate(R.layout.item_order, viewGroup))
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        orders[position]?.let { holder.bind(it) }
    }

    override fun getItemCount(): Int {
        return orders.size
    }

    //endregion OVERRIDE

    //region ******************** HOLDER ***********************************************************

    open inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.tvName
        private val message: TextView = itemView.tvMessage
        private val addressHeader: TextView = itemView.tvAddressHeader
        private val address: TextView = itemView.tvAddress
        private val date: TextView = itemView.tvDate
        private val marker: ImageView = itemView.marker
        private val showReceiptButton: View = itemView.linearLayoutShowReceipt
        private val qrButton: View = itemView.scanQrCode

        fun bind(order: Product) {
            with(title) {
                text = order.medicineName
                setOnClickListener { itemClickListener.click(order, it.context) }
            }
            val state = order.state
            message.text = order.currentStateMessage(PackageState.valueOf(state!!).ordinal)
            qrButton.setOnClickListener { qrCodeClickListener.click(order, it.context) }
            showReceiptButton.setOnClickListener { receiptClickListner.click(order, showReceiptButton.context) }

            if (PackageState.ISSUED.name == state || PackageState.PROCESSED.name == state) {
                qrButton.gone()
                showReceiptButton.visible()
                addressHeader.gone()
                address.gone()
                marker.gone()
            } else if (PackageState.DELIVERED.name == state) {
                qrButton.visible()
                showReceiptButton.visible()
                addressHeader.gone()
                address.gone()
                marker.gone()
            } else {
                qrButton.visible()
                showReceiptButton.visible()
                address.visible()
                addressHeader.visible()
                marker.visible()
            }
            date.text = parseDateTime(order.deliveredAt
                ?: order.processedAt ?: order.issuedAt
                ?: System.currentTimeMillis(), dateFormatter)
        }
    }
    //endregion HOLDER
}
