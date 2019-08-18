package com.luxoft.supplychain.sovrinagentapp.utils

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import com.luxoft.supplychain.sovrinagentapp.R
import rx.Observable
import java.util.concurrent.TimeUnit

fun showPopup(header: String, message: String, autoDismiss: Boolean, context: Context) {
    //TODO to notification
    val dialog = Dialog(context)
    dialog.setContentView(R.layout.popup_layout)
    val textViewPopupHeader: TextView = dialog.findViewById(R.id.tvHeader)
    val textViewPopupMessage: TextView = dialog.findViewById(R.id.tvMessage)
    textViewPopupHeader.text = header
    textViewPopupMessage.text = message
    dialog.window?.let {
        it.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        it.setGravity(Gravity.TOP)
    }
    dialog.show()

    if (autoDismiss) {
        Observable.timer(10, TimeUnit.SECONDS).subscribe { dialog.dismiss() }
    }
}