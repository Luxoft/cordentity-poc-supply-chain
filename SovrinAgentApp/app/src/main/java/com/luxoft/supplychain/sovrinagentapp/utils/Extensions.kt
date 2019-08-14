package com.luxoft.supplychain.sovrinagentapp.utils

import android.content.Context
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

fun Context.inflate(@LayoutRes res: Int, parent: ViewGroup? = null) : View {
    return LayoutInflater.from(this).inflate(res, parent, false)
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}