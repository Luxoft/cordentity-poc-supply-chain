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