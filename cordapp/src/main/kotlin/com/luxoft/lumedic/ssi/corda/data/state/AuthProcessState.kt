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

package com.luxoft.lumedic.ssi.corda.data.state

import com.luxoft.lumedic.ssi.corda.contract.ToDoContract
import com.luxoft.lumedic.ssi.corda.data.AuthResponse
import com.luxoft.lumedic.ssi.corda.data.AuthState
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty

@BelongsToContract(ToDoContract::class)
data class AuthProcessState(
    val authInfo: AuthResponse,
    val authState: AuthState,
    val patientDid: String?,
    val owner: AbstractParty,
    override val participants: List<AbstractParty> = listOf(owner),
    override val linearId: UniqueIdentifier = UniqueIdentifier(externalId = authInfo.requestId)
) : LinearState
