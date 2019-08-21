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
import com.luxoft.supplychain.sovrinagentapp.application.SERIAL
import com.luxoft.supplychain.sovrinagentapp.application.STATE
import com.luxoft.supplychain.sovrinagentapp.data.PackageState
import com.luxoft.supplychain.sovrinagentapp.data.Product
import com.luxoft.supplychain.sovrinagentapp.ui.activities.DigitalReceiptActivity
import com.luxoft.supplychain.sovrinagentapp.ui.activities.SimpleScannerActivity
import com.luxoft.supplychain.sovrinagentapp.ui.activities.TrackPackageActivity
import com.luxoft.supplychain.sovrinagentapp.utils.DateTimeUtils
import com.luxoft.supplychain.sovrinagentapp.utils.gone
import com.luxoft.supplychain.sovrinagentapp.utils.inflate
import com.luxoft.supplychain.sovrinagentapp.utils.visible
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.block_medicine_info.view.*
import kotlinx.android.synthetic.main.item_order.view.*

class OrdersAdapter(realm: Realm) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

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

    //region ******************** HOLDERS **********************************************************

    open inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.tvName
        var message: TextView = itemView.tvMessage
        var addressHeader: TextView = itemView.tvAddressHeader
        var address: TextView = itemView.tvAddress
        var date: TextView = itemView.tvDate
        var marker: ImageView = itemView.marker
        var showReceiptButton: View = itemView.linearLayoutShowReceipt
        var qrButton: View = itemView.scanQrCode

        fun bind(order: Product) {
            title.text = order.medicineName
            message.text = order.currentStateMessage(PackageState.valueOf(order.state!!).ordinal)
            title.setOnClickListener {
                startActivity(title.context, Intent().setClass(title.context, TrackPackageActivity::class.java).putExtra(SERIAL, order.serial), null)
            }
            qrButton.setOnClickListener {
                ContextCompat.startActivity(qrButton.context,
                    Intent().setClass(qrButton.context, SimpleScannerActivity::class.java)
                        .putExtra(SERIAL, order.serial)
                        .putExtra(STATE, order.state), null
                )
            }
            showReceiptButton.setOnClickListener {
                ContextCompat.startActivity(showReceiptButton.context,
                    Intent().setClass(showReceiptButton.context, DigitalReceiptActivity::class.java)
                        .putExtra(SERIAL, order.serial)
                        .putExtra(STATE, order.state), null
                )
            }
            if (order.state.equals(PackageState.ISSUED.name) || order.state.equals(PackageState.PROCESSED.name)) {
                qrButton.gone()
                showReceiptButton.visible()
                addressHeader.gone()
                address.gone()
                marker.gone()
            }
            if (order.state.equals(PackageState.DELIVERED.name)) {
                qrButton.visible()
                showReceiptButton.visible()
                addressHeader.gone()
                address.gone()
                marker.gone()
            }
            else {
                address.visible()
                addressHeader.visible()
                marker.visible()
            }
            date.text = DateTimeUtils.parseDateTime(order.deliveredAt
                ?: order.processedAt ?: order.issuedAt
                ?: System.currentTimeMillis(), "dd MMM yyyy")
        }
    }
    //endregion HOLDERS
}
