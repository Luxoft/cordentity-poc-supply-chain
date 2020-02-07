package com.luxoft.supplychain.sovrinagentapp.data

import android.content.Context

class SharedPreferencesStore(val context: Context) {
//    lateinit var context: Context

    fun writeString(name: String, key: String, value: String) {
        context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().putString(key, value).apply()
    }

    fun readString(name: String, key: String): String? =
            context.getSharedPreferences(name, Context.MODE_PRIVATE).getString(key, "")

}