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
        return PackageState.valueOf(product.state!!).ordinal
    }


    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int) {

        var position1 = position.inc()
        val timestamp = product.currentStateTimestamp(position1)
        val message = product.currentStateMessage(position1)

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
