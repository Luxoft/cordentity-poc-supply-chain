package com.luxoft.supplychain.sovrinagentapp.ui.model

import android.content.Intent
import android.support.v4.content.ContextCompat
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
import com.luxoft.supplychain.sovrinagentapp.ui.MainActivity
import com.luxoft.supplychain.sovrinagentapp.ui.SimpleScannerActivity
import com.luxoft.supplychain.sovrinagentapp.ui.TrackPackageActivity
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.order_list_item.view.*
import kotlinx.android.synthetic.main.qr_list_item.view.*


class OrdersAdapter(realm: Realm) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var realmChangeListener = RealmChangeListener<Realm> {
        Log.i("TAG", "Change occurred!")
        this.notifyDataSetChanged()
    }

    init {
        realm.addChangeListener(realmChangeListener)
    }

    private val orders: RealmResults<Product> = realm.where(Product::class.java).sort("requestedAt", Sort.DESCENDING).findAll()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val order = orders[position]

        if(order == null) {
            Log.i("TAG", "Item not found for index $position")
        } else when(holder) {
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
        holder.qrButton.setOnClickListener {
            ContextCompat.startActivity(holder.qrButton.context,
                    Intent().setClass(holder.qrButton.context, SimpleScannerActivity::class.java)
                            .putExtra("serial", order.serial)
                            .putExtra("state", order.state), null
            )
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

        return if(viewType == QR) {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.qr_list_item, viewGroup, false)
            QROrderViewHolder(view)
        } else {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.order_list_item, viewGroup, false)
            OrderViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return orders.size
    }

    override fun getItemViewType(position: Int): Int {
        val order = orders[position]
        return if(order?.state == PackageState.NEW.name || order?.state == PackageState.DELIVERED.name) QR else plain
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