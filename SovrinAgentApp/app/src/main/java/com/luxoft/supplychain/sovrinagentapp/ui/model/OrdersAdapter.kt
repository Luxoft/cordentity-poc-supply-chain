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
import com.luxoft.supplychain.sovrinagentapp.application.SERIAL
import com.luxoft.supplychain.sovrinagentapp.data.PackageState
import com.luxoft.supplychain.sovrinagentapp.data.Product
import com.luxoft.supplychain.sovrinagentapp.ui.*
import com.luxoft.supplychain.sovrinagentapp.utils.DateTimeUtils
import com.luxoft.supplychain.sovrinagentapp.utils.gone
import com.luxoft.supplychain.sovrinagentapp.utils.inflate
import com.luxoft.supplychain.sovrinagentapp.utils.visible
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort
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
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int {
        return orders.size
    }

    //endregion OVERRIDE

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
                title.text = it.medicineName
//                sn.text = "SN: " + order.serial
                message.text = it.currentStateMessage(PackageState.valueOf(it.state!!).ordinal)
                title.setOnClickListener {
                    startActivity(title.context, Intent().setClass(title.context, TrackPackageActivity::class.java).putExtra(SERIAL, order.serial), null)
                }
                qrButton.setOnClickListener {
                    ContextCompat.startActivity(qrButton.context,
                        Intent().setClass(qrButton.context, SimpleScannerActivity::class.java)
                            .putExtra(SERIAL, order.serial)
                            .putExtra("state", order.state), null
                    )
                }
                showReceiptButton.setOnClickListener {
                    ContextCompat.startActivity(showReceiptButton.context,
                        Intent().setClass(showReceiptButton.context, DigitalReceiptActivity::class.java)
                            .putExtra(SERIAL, order.serial)
                            .putExtra("state", order.state), null
                    )
                }
                if (it.state.equals(PackageState.ISSUED.name) || it.state.equals(PackageState.PROCESSED.name)) {
                    qrButton.gone()
                    showReceiptButton.visible()
                    textViewAddressHeader.gone()
                    textViewAddress.gone()
                    imageViewMapMarker.gone()
                }
                if (order.state.equals(PackageState.DELIVERED.name)) {
                    qrButton.visible()
                    showReceiptButton.visible()
                    textViewAddressHeader.gone()
                    textViewAddress.gone()
                    imageViewMapMarker.gone()
                }
                textViewOrderItemDate.text =  DateTimeUtils.parseDateTime (it.deliveredAt
                    ?: it.processedAt ?: it.issuedAt
                    ?: System.currentTimeMillis(), "dd MMM yyyy")
            }
        }
    }
    //endregion HOLDERS
}
