package com.luxoft.supplychain.sovrinagentapp.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import androidx.lifecycle.LiveData
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.data.CredentialAttributePresentationRules
import com.luxoft.supplychain.sovrinagentapp.data.VerificationEvent
import kotlinx.android.synthetic.main.item_verification.view.verificationDate
import kotlinx.android.synthetic.main.item_verification.view.verifierName
import kotlinx.android.synthetic.main.item_verification_expanded.view.*
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class VerificationsHistoryAdapter(val context: Context, history: LiveData<List<VerificationEvent>>) :
    BaseExpandableListAdapter(), KoinComponent
{
    private var items: List<VerificationEvent> = listOf()

    init {
        // todo: observe on lifecycle
        history.observeForever { list ->
            items = list
            notifyDataSetChanged()
        }
    }

    private val attributeFormatter: CredentialAttributePresentationRules by inject()
    private val inflater = LayoutInflater.from(context)

    override fun getGroup(groupPosition: Int): Any = items[groupPosition]
    override fun getChild(groupPosition: Int, childPosition: Int): Any = TODO()
    override fun getGroupCount(): Int = items.size
    override fun getChildrenCount(groupPosition: Int): Int = 0

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()
    override fun getChildId(groupPosition: Int, childPosition: Int): Long = 0

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = false
    override fun hasStableIds(): Boolean = false // todo: what is it?

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val event = items[groupPosition]

        return if(isExpanded)
            groupViewExpanded(convertView, parent, event)
        else
            groupViewCollapsed(convertView, parent, event)
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        return TODO()
    }

    private fun groupViewCollapsed(convertView: View?, parent: ViewGroup?, event: VerificationEvent): View {
        // todo: re-use [convertView]
        val view = inflater.inflate(R.layout.item_verification, /*root=*/parent, /*attachTORoot=*/false)

        val date = Date.from(event.verificationInstant)
        val dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.US)
        val timeFormat = SimpleDateFormat("hh:mm aa", Locale.US)

        view.verificationDate.text = timeFormat.format(date) + "  " + dateFormat.format(date)
        view.verifierName.text = event.verifier.name

        return view
    }

    private fun groupViewExpanded(convertView: View?, parent: ViewGroup?, event: VerificationEvent): View {
        // todo: re-use [convertView]
        val view = inflater.inflate(R.layout.item_verification_expanded, /*root=*/parent, /*attachTORoot=*/false)

        val date = Date.from(event.verificationInstant)
        val dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.US)
        val timeFormat = SimpleDateFormat("hh:mm aa", Locale.US)

        view.verificationDate.text = timeFormat.format(date) + "  " + dateFormat.format(date)
        view.verifierName.text = event.verifier.name
        view.verifierAddress.text = event.verifier.address
        view.verifierContactPhone.text = event.verifier.contactPhone

        return view
    }
}