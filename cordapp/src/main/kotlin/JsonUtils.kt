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

import ch.qos.logback.classic.Level
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.KotlinModule

/**
 * Object that makes serialization simplier
 */
object SerializationUtils {
    val mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

    init {
        mapper.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")
        /*(org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger)
                .level = Level.TRACE*/
    }

    fun anyToJSON(obj: Any?): String = mapper.writeValueAsString(obj)
    fun anyToBytes(obj: Any?): ByteArray = mapper.writeValueAsBytes(obj)

    inline fun <reified T> jSONToAny(json: String): T? = mapper.readValue(json, T::class.java)
    inline fun <reified T> bytesToAny(bytes: ByteArray): T? = mapper.readValue(bytes, T::class.java)

    fun <T> jSONToAny(json: String, clazz: Class<T>): T? = mapper.readValue(json, clazz)
    fun <T> bytesToAny(bytes: ByteArray, clazz: Class<T>): T? = mapper.readValue(bytes, clazz)
}
