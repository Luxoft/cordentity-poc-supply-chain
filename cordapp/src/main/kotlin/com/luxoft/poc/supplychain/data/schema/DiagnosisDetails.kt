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

import com.luxoft.poc.supplychain.data.schema.IndySchemaBuilder.AttrTypes
import com.luxoft.poc.supplychain.data.schema.IndySchemaBuilder.Attribute

object DiagnosisDetails: IndySchema(schemaName = "medicine_diagnosis", schemaVersion = "1.0") {

    object Attributes {
        object Stage : AttrTypes by Attribute("stage")
        object Disease : AttrTypes by Attribute("disease")
        object MedicineName : AttrTypes by Attribute("medicineName")
        object Recommendation : AttrTypes by Attribute("recommendation")
    }

    override fun getSchemaAttrs(): List<AttrTypes> = listOf(
            Attributes.Stage,
            Attributes.Disease,
            Attributes.MedicineName,
            Attributes.Recommendation
    )
}
