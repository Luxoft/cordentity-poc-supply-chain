package com.luxoft.supplychain.sovrinagentapp.ui.timeline


import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.github.vipulasri.timelineview.TimelineView
import kotlinx.android.synthetic.main.item_timeline.view.*

class TimeLineViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {

    var mDate: TextView = itemView.text_timeline_date
    var mMessage: TextView = itemView.text_timeline_title
    var mTimelineView: TimelineView = itemView.time_marker

    init {
        mTimelineView.initLine(viewType)
    }
}