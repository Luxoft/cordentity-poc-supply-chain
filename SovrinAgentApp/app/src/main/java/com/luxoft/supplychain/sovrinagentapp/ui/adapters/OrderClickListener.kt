package com.luxoft.supplychain.sovrinagentapp.ui.adapters

import android.content.Context
import com.luxoft.supplychain.sovrinagentapp.data.Product

interface OrderClickListener {
    fun click(order: Product, context: Context)
}