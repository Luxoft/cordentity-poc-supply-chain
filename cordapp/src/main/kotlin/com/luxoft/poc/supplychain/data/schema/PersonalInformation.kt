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

object PersonalInformation: IndySchema(schemaName = "personal_information", schemaVersion = "1.0") {
    object Attributes {
        object Nationality : IndySchemaBuilder.AttrTypes by IndySchemaBuilder.Attribute("nationality")
        object Forename : IndySchemaBuilder.AttrTypes by IndySchemaBuilder.Attribute("forename")
        object Age : IndySchemaBuilder.AttrTypes by IndySchemaBuilder.Attribute("age")
    }

    override fun getSchemaAttrs(): List<IndySchemaBuilder.AttrTypes> = listOf(
        Attributes.Age,
        Attributes.Nationality,
        Attributes.Forename
    )
}
