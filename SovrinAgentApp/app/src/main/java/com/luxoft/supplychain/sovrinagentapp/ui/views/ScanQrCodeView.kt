package com.luxoft.supplychain.sovrinagentapp.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.luxoft.supplychain.sovrinagentapp.R
import kotlinx.android.synthetic.main.block_scan_qr_code.view.*

class ScanQrCodeView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    init {
        LayoutInflater.from(context).inflate(R.layout.block_scan_qr_code, this, true)
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScanQrCodeView)
            tvTitle.text = typedArray.getString(R.styleable.ScanQrCodeView_qr_code_title)
            tvText.text = typedArray.getString(R.styleable.ScanQrCodeView_qr_code_text)
            typedArray.recycle()
        }
    }
}