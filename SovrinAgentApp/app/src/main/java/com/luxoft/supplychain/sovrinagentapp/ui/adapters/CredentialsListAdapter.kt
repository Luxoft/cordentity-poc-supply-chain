package com.luxoft.supplychain.sovrinagentapp.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import androidx.lifecycle.LiveData
import com.luxoft.blockchainlab.hyperledger.indy.models.CredentialReference
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.data.ApplicationState
import kotlinx.android.synthetic.main.item_credential_attribute.view.*
import kotlinx.android.synthetic.main.item_credentilal.view.*
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class CredentialsListAdapter(val context: Context, credentials: LiveData<List<CredentialReference>>) :
    BaseExpandableListAdapter(), KoinComponent
{
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

    private val appState: ApplicationState by inject()
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getGroup(groupPosition: Int): Any = groups[groupPosition]
    override fun getChild(groupPosition: Int, childPosition: Int): Any = items[groupPosition][childPosition]
    override fun getGroupCount(): Int = groups.size
    override fun getChildrenCount(groupPosition: Int): Int = items[groupPosition].size

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()
    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = false
    override fun hasStableIds(): Boolean = false // todo: what is it?

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val cred = groups[groupPosition]

        // todo: reuse [convertView]
        // todo: maybe use parent view as root?
        val view = inflater.inflate(R.layout.item_credentilal, /*root=*/null)

        val formatter = appState.credentialPresentationRules

        view.tittle.text = formatter.formatName(cred)
        view.description.text = formatter.formatDescription(cred)
        view.verifier.text = formatter.formatIssuerName(cred)

        return view
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val (key, value) = items[groupPosition][childPosition]

        // todo: reuse [convertView]
        // todo: maybe use parent view as root?
        val view = inflater.inflate(R.layout.item_credential_attribute, /*root=*/null)

        val formatter = appState.credentialAttributePresentationRules

        view.name.text = formatter.formatName(key)
        view.textValue.text = formatter.formatValueText(key, value, maxWidth = 45)

        /* Handle img attribute:
        item.value ?: return
        val imageBytes = Base64.getDecoder().decode(item.value)

        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        value.setImageBitmap(bitmap)
        * */

        return view
    }

}