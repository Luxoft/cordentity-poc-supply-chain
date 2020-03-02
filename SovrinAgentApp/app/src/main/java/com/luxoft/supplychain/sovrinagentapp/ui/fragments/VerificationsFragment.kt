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

package com.luxoft.supplychain.sovrinagentapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.data.ApplicationState
import com.luxoft.supplychain.sovrinagentapp.data.PackageState
import com.luxoft.supplychain.sovrinagentapp.ui.activities.SimpleScannerActivity
import com.luxoft.supplychain.sovrinagentapp.ui.adapters.VerificationsHistoryAdapter
import kotlinx.android.synthetic.main.fragment_verifications.*
import org.koin.android.ext.android.inject


class VerificationsFragment : Fragment() {

    private val appState: ApplicationState by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_verifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        appState.authenticationHistory.observe({lifecycle}) { verifications ->
            if(verifications.isEmpty()) {
                tipPreseentCredentioal.visibility = View.VISIBLE
            } else {
                tipPreseentCredentioal.visibility = View.GONE
            }
        }

        scanVerificationRequest.setOnClickListener {
            val intent = Intent()
                .setClass(scanVerificationRequest.context, SimpleScannerActivity::class.java)
                .putExtra("serial", 0) // not used
                .putExtra("state", PackageState.NEW.name)

            ContextCompat.startActivity(scanVerificationRequest.context, intent, /*options=*/null)
        }

        verificationHistory.adapter = VerificationsHistoryAdapter(requireContext(), appState.authenticationHistory)
    }
}