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

package com.luxoft.poc.supplychain.data.schema

import java.util.*

open class IndySchema(val schemaName: String, val schemaVersion: String, val attributes: List<String>) {
    override fun toString(): String = "${schemaName}:${schemaVersion}"
}

val namePostFix = "${Math.abs(Random().nextInt())}"
val version = "${Math.abs(Random().nextInt())}.${Math.abs(Random().nextInt())}.${Math.abs(Random().nextInt())}"

object PackageIndySchema : IndySchema(
    "package_receipt-$namePostFix",
    version,
    listOf(
        "serial",
        "authorities",
        "time"
    )
)

object CertificateIndySchema : IndySchema(
    "certificate-$namePostFix",
    version,
    listOf(
        "serial",
        "status",
        "time"
    )
)
