package com.luxoft.supplychain.sovrinagentapp.utils


import android.text.format.DateUtils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    fun parseDateTime(timestamp: Long, outputFormat: String): String {

        var date = Date(timestamp)
        return try {
            val dateFormat = SimpleDateFormat(outputFormat, Locale("US"))
            dateFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            ""
        }
    }

    fun getRelativeTimeSpan(dateString: String, originalFormat: String): String {

        val formatter = SimpleDateFormat(originalFormat, Locale.US)
        var date: Date? = null
        try {
            date = formatter.parse(dateString)

            return DateUtils.getRelativeTimeSpanString(date!!.time).toString()

        } catch (e: ParseException) {
            e.printStackTrace()
            return ""
        }

    }
}