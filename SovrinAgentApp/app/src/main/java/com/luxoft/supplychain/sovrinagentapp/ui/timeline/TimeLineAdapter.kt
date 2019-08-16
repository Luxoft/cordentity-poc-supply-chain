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

package com.luxoft.supplychain.sovrinagentapp.ui.timeline

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.vipulasri.timelineview.TimelineView
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.data.PackageState
import com.luxoft.supplychain.sovrinagentapp.data.Product
import com.luxoft.supplychain.sovrinagentapp.utils.DateTimeUtils
import com.luxoft.supplychain.sovrinagentapp.utils.gone
import com.luxoft.supplychain.sovrinagentapp.utils.inflate
import com.luxoft.supplychain.sovrinagentapp.utils.visible
import kotlinx.android.synthetic.main.item_timeline.view.*

class TimeLineAdapter(private val product: Product) : RecyclerView.Adapter<TimeLineAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(parent.context.inflate(R.layout.item_timeline, parent), viewType)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val stateOrder = position.inc()
        val timestamp = product.currentStateTimestamp(stateOrder)
        val message = product.currentStateMessage(stateOrder)

        if (timestamp != null) {
            holder.mDate.visible()
            holder.mDate.text = DateTimeUtils.parseDateTime(timestamp, "MMM dd, yyyy HH:mm")
        } else {
            holder.mDate.gone()
        }

        holder.mMessage.text = message
    }

    override fun getItemCount(): Int {
        return PackageState.valueOf(product.state!!).ordinal
    }

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }

    class Holder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {

        var mDate: TextView = itemView.text_timeline_date
        var mMessage: TextView = itemView.text_timeline_title
        var mTimelineView: TimelineView = itemView.time_marker

        init {
            mTimelineView.initLine(viewType)
        }
    }
}
