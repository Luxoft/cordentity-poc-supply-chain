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

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.data.ClaimAttribute
import com.luxoft.supplychain.sovrinagentapp.utils.inflate
import io.realm.RealmChangeListener
import io.realm.RealmResults
import kotlinx.android.synthetic.main.item_claim.view.*

class ClaimsAdapter(private val claims: RealmResults<ClaimAttribute>) : RecyclerView.Adapter<ClaimsAdapter.ClaimViewHolder>() {

    var realmChangeListener = RealmChangeListener<RealmResults<ClaimAttribute>> {
        Log.i("TAG", "Change occurred!")
        this.notifyDataSetChanged()
    }

    init {
        claims.addChangeListener(realmChangeListener)
    }

    //region ******************** OVERRIDE *************************************************************

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ClaimViewHolder {
        return ClaimViewHolder(viewGroup.context.inflate(R.layout.item_claim, viewGroup))
    }

    override fun onBindViewHolder(holder: ClaimViewHolder, position: Int) {
        holder.bind(claims[position])
    }

    override fun getItemCount(): Int {
        return claims.size
    }

    //endregion OVERRIDE

    //region ******************** HOLDER ***********************************************************

    inner class ClaimViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var name: TextView = itemView.tv_name
        var value: TextView = itemView.tv_value
        var schemaId: TextView = itemView.tv_schema_id

        fun bind(item: ClaimAttribute?) {
            item?.let {
                name.text = it.key
                value.text = it.value
                schemaId.text = it.schemaId
            }
        }
    }

    //endregion HOLDER
}
