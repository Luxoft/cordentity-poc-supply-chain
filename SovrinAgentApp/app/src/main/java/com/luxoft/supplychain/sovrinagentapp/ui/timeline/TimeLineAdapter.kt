package com.luxoft.supplychain.sovrinagentapp.ui.timeline

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.vipulasri.timelineview.TimelineView
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.data.PackageState
import com.luxoft.supplychain.sovrinagentapp.data.Product
import com.luxoft.supplychain.sovrinagentapp.utils.DateTimeUtils


class TimeLineAdapter(private val product: Product) : RecyclerView.Adapter<TimeLineViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_timeline, parent, false)
        return TimeLineViewHolder(view, viewType)
    }

    override fun getItemCount(): Int {
        return PackageState.valueOf(product.state!!).ordinal + 1
    }


    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int) {

        val timestamp = product.currentStateTimestamp(position)
        val message = product.currentStateMessage(position)

        if (timestamp != null) {
            holder.mDate.visibility = View.VISIBLE
            holder.mDate.text = DateTimeUtils.parseDateTime(timestamp, "MMM dd, yyyy HH:mm")
        } else
            holder.mDate.visibility = View.GONE

        holder.mMessage.text = message
    }

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }
}