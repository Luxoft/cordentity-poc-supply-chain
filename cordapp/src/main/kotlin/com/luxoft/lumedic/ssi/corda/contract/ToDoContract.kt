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

package com.luxoft.lumedic.ssi.corda.contract

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

open class ToDoContract : Contract {
    companion object {
        val ID: String = this::class.java.enclosingClass.name
    }

    override fun verify(tx: LedgerTransaction) {
        val commands = tx.commandsOfType<Commands>()
        val inputs = tx.inputsOfType<ContractState>()
        val outputs = tx.outputsOfType<ContractState>()
        requireThat {
            "Single $ID command required" using (commands.size == 1)
            val command = commands.single()
            when (command.value) {
                is Commands.Do -> {
                }
                else ->
                    throw IllegalStateException("Unsupported $ID command ${command.value}")
            }
        }
    }

    interface Commands : CommandData {
        class Do : Commands, TypeOnlyCommandData()
    }
}
