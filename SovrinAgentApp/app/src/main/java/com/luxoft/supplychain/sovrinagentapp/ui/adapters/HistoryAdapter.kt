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
import android.util.SparseBooleanArray
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.application.SERIAL
import com.luxoft.supplychain.sovrinagentapp.application.STATE
import com.luxoft.supplychain.sovrinagentapp.data.PackageState
import com.luxoft.supplychain.sovrinagentapp.data.Product
import com.luxoft.supplychain.sovrinagentapp.data.ProductOperation
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
import kotlinx.android.synthetic.main.item_history.view.*
import kotlinx.android.synthetic.main.item_history_content.view.*

class HistoryAdapter(realm: Realm) : RecyclerView.Adapter<HistoryAdapter.OrderViewHolder>() {

    private val orders: RealmResults<Product> = realm.where(Product::class.java).sort("collectedAt", Sort.DESCENDING).isNotNull("collectedAt").findAll()
    private val productOperations: RealmResults<ProductOperation> = realm.where(ProductOperation::class.java).findAll()

    private val itemsState = SparseBooleanArray()

    private val qrCodeClickListener = object : OrderClickListener{
        override fun click(order: Product, context: Context) {
            ContextCompat.startActivity(context,
                Intent().setClass(context, SimpleScannerActivity::class.java)
                    .putExtra("collected_at", order.collectedAt)
                    .putExtra(SERIAL, order.serial)
                    .putExtra(STATE, order.state), null
            )
        }
    }

    private val itemClickListener = object : OrderClickListener{
        override fun click(order: Product, context: Context) {
            startActivity(context, Intent().setClass(context, TrackPackageActivity::class.java).putExtra(SERIAL, order.serial), null)
        }
    }

    private var realmChangeListener = RealmChangeListener<Realm> {
        Log.i("TAG", "Change occurred!")
        this.notifyDataSetChanged()
    }

    init {
        realm.addChangeListener(realmChangeListener)
    }

    //region ******************** OVERRIDE *********************************************************

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(viewGroup.context.inflate(R.layout.item_history, viewGroup))
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        orders[position]?.let { holder.bind(it, position) }
    }

    override fun getItemCount(): Int {
        return orders.size
    }

    //endregion OVERRIDE

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.tvName
        var message: TextView = itemView.tvMessage
        var date: TextView = itemView.tvDate
        var qrButton: View = itemView.scanQrCode
        var historyContent: LinearLayout = itemView.linearLayoutHistoryContent
        var licenseList: LinearLayout = itemView.linearLayoutLicenseList
        var expandContainer: LinearLayout = itemView.linearLayoutExpand
        var image: ImageView = itemView.imageViewExpand

        fun bind(order: Product, position: Int) {
            title.text = order.medicineName
            message.text = order.currentStateMessage(PackageState.valueOf(order.state!!).ordinal)
            title.setOnClickListener {
                itemClickListener.click(order, it.context)
            }
            qrButton.setOnClickListener {
                qrCodeClickListener.click(order, it.context)
            }

            historyContent.removeAllViews()
            qrButton.visible()
            licenseList.gone()
            for (productOperation in productOperations) {
                if (productOperation.at!! == order.collectedAt && productOperation.by.equals("approved")) {
                    qrButton.gone()
                    licenseList.visible()
                    fillLicenseList()
                    if (itemsState.get(position)) expandLicenseList()
                    else collapseLicenseList()

                    expandContainer.setOnClickListener {
                        if (historyContent.visibility == View.GONE) {
                            itemsState.append(position, true)
                            expandLicenseList()
                        } else {
                            itemsState.append(position, false)
                            collapseLicenseList()
                        }
                    }
                    break
                }
            }
            date.text = DateTimeUtils.parseDateTime(order.collectedAt!!, "dd MMM yyyy HH:mm:ss")
        }

        private fun expandLicenseList() {
            historyContent.visible()
            image.setImageDrawable(image.context.getDrawable(R.drawable.up))
        }

        private fun collapseLicenseList() {
            historyContent.gone()
            image.setImageDrawable(image.context.getDrawable(R.drawable.down))
        }

        private fun fillLicenseList() {
            fun createView(title: String, text: String): View {
                val result = itemView.context.inflate(R.layout.item_history_content)
                result.tv_item_history_title.text = title
                result.tv_item_history_name.text = text

                return result
            }

            historyContent.addView(createView("DID LICENSE", "09928390239TYDVCHD8999"))
            historyContent.addView(createView("AUTHORITY", "TC SEEHOF"))
            historyContent.addView(createView("MANUFACTURE", "Manufacturing Astura 673434"))

        }
    }

    private interface OrderClickListener {
        fun click(order: Product, context: Context)
    }
}
