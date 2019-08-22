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

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.vipulasri.timelineview.TimelineView
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.data.PackageState
import com.luxoft.supplychain.sovrinagentapp.data.Product
import com.luxoft.supplychain.sovrinagentapp.utils.*
import kotlinx.android.synthetic.main.item_timeline.view.*

class TimeLineAdapter(private val product: Product) : RecyclerView.Adapter<TimeLineAdapter.Holder>() {

    private val dateFormatter = createFormatter("MMM dd, yyyy HH:mm")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(parent.context.inflate(R.layout.item_timeline, parent))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val stateOrder = position.inc()
        val timestamp = product.currentStateTimestamp(stateOrder)
        val message = product.currentStateMessage(stateOrder)
        val lineType = TimelineView.getTimeLineViewType(position, itemCount)

        holder.bind(timestamp, message, lineType)
    }

    override fun getItemCount(): Int {
        return PackageState.valueOf(product.state!!).ordinal
    }

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvDate: TextView = itemView.text_timeline_date
        private val tvMessage: TextView = itemView.text_timeline_title
        private val timelineView: TimelineView = itemView.time_marker

        fun bind(time: Long?, message: String?, lineType: Int) {
            if (time != null) {
                tvDate.visible()
                tvDate.text = parseDateTime(time, dateFormatter)
            } else {
                tvDate.gone()
            }

            tvMessage.text = message
            timelineView.initLine(lineType)
        }
    }
}
