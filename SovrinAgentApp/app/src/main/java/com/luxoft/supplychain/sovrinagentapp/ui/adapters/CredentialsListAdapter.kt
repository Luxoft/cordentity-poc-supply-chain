package com.luxoft.supplychain.sovrinagentapp.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import androidx.lifecycle.LiveData
import com.luxoft.blockchainlab.hyperledger.indy.models.CredentialReference
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.data.ClaimAttribute
import kotlinx.android.synthetic.main.item_credential_attribute.view.*
import kotlinx.android.synthetic.main.item_credentilal.view.*
import org.apache.commons.lang3.StringUtils

class CredentialsListAdapter(val context: Context, val credentials: LiveData<List<CredentialReference>>) : BaseExpandableListAdapter() {

    private var groups: List<CredentialReference> = listOf()
    private var items: List<List<Pair<String, String>>> = listOf()

    init {
        credentials.observeForever { creds ->
            groups = creds
            items = creds.map { cred ->
                cred.attributes.map { (k, v) -> Pair(k, v.toString()) }
            }
        }
    }

    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getGroup(groupPosition: Int): Any = groups[groupPosition]
    override fun getChild(groupPosition: Int, childPosition: Int): Any = items[groupPosition][childPosition]
    override fun getGroupCount(): Int = groups.size
    override fun getChildrenCount(groupPosition: Int): Int = items[groupPosition].size

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()
    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = false
    override fun hasStableIds(): Boolean = true // todo: what is it?

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val cred = groups[groupPosition]
        val schemaId = cred.getSchemaIdObject()
        val credDefId = cred.getCredentialDefinitionIdObject()

        // todo: reuse [convertView]
        // todo: maybe use parent view as root?
        val view = inflater.inflate(R.layout.item_credentilal, /*root=*/null)

        view.tittle.text = schemaId.name
        view.description.text = cred.referent
        view.verifier.text = credDefId.tag

        return view
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val (name, content) = items[groupPosition][childPosition]

        // todo: reuse [convertView]
        // todo: maybe use parent view as root?
        val view = inflater.inflate(R.layout.item_credential_attribute, /*root=*/null)

        view.name.text = name
        view.textValue.text = content

        /* Handle img attribute:
        item.value ?: return
        val imageBytes = Base64.getDecoder().decode(item.value)

        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        value.setImageBitmap(bitmap)
        * */

        return view
    }

}


private fun ClaimAttribute.prettyKey(): String = StringUtils.abbreviate(key ?: "---", 30)
private fun ClaimAttribute.prettyValue(): String = StringUtils.abbreviate(value ?: "null", 512)
private fun ClaimAttribute.prettySchema(): String = "$schemaName:$schemaVersion"