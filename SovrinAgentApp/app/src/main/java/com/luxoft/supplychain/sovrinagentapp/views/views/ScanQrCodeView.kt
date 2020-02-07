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

package com.luxoft.supplychain.sovrinagentapp.views.views

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