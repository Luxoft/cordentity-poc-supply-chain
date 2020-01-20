package com.luxoft.supplychain.sovrinagentapp.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.lifecycle.LiveData
import com.luxoft.blockchainlab.hyperledger.indy.models.ProofInfo
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.data.ApplicationState
import kotlinx.android.synthetic.main.item_verification.view.*
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.text.DateFormat
import java.time.Instant
import java.util.*

class VerificationsHistoryAdapter(val context: Context, history: LiveData<List<ProofInfo>>) :
    BaseAdapter(), KoinComponent
{
    private var items: List<ProofInfo> = listOf()

    init {
        history.observeForever { list ->
            items = list
        }
    }

    private val appState: ApplicationState by inject()
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getItem(position: Int): Any = items[position]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getCount(): Int = items.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val proofInfo = items[position]

        // todo: re-use [convertView]
        // todo: maybe use parent view as root?
        val view = inflater.inflate(R.layout.item_verification, /*root=*/null)

        val verificationDate = Instant.now()

        val credentialSet = setOf("Insurance and Subscriber Data Elements", "Patient Demographics").map { schemaName ->
            appState.credentialPresentationRules.formatName(schemaName)
        }

        view.verificationDate.text = DateFormat.getDateInstance().format(Date.from(verificationDate))
        view.credentialSet.text = credentialSet.joinToString()

        return view
    }

}