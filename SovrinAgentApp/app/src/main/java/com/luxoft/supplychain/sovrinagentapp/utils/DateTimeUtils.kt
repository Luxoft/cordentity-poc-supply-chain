/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

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
