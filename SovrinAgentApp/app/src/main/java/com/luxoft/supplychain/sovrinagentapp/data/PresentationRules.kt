package com.luxoft.supplychain.sovrinagentapp.data

import com.luxoft.blockchainlab.hyperledger.indy.models.CredentialReference
import com.luxoft.supplychain.sovrinagentapp.data.KnownSchemas.demographicsSchemaName
import com.luxoft.supplychain.sovrinagentapp.data.KnownSchemas.insuranceSchemaName
import java.lang.Integer.min
import java.text.DateFormat
import java.util.*

object KnownSchemas {
    val insuranceSchemaName = "Insurance and Subscriber Data Elements"
    val demographicsSchemaName = "Patient Demographics"

    val schemaNames = listOf(insuranceSchemaName, demographicsSchemaName)
}

class CredentialPresentationRules() {

    fun formatName(cred: CredentialReference): String = formatName(cred.getSchemaIdObject().name)

    fun formatName(schemaName: String): String {
        return when(schemaName) {
            insuranceSchemaName -> "Health Member Plan"
            demographicsSchemaName -> "Personal Digital ID"
            else -> schemaName
        }
    }

    fun formatDescription(cred: CredentialReference): String? {
        return when(cred.getSchemaIdObject().name) {
            insuranceSchemaName -> "Health Plan"
            demographicsSchemaName -> cred.attributes["Full_legal_name"]?.toString()
            else -> null
        }
    }

    fun formatIssuerName(cred: CredentialReference): String? {
        return when(cred.getSchemaIdObject().name) {
            insuranceSchemaName -> cred.attributes["Insurance_company_name"]?.toString()
            demographicsSchemaName -> "U.S. Department of State"
            else -> null
        }
    }
}

class CredentialAttributePresentationRules() {
    enum class ContentType { TEXT, DATE, PIC }

    fun contentType(attrKey: String, attrValue: Any?): ContentType {
        val content = attrValue?.toString()

        return when {
            attrKey.endsWith("_ms") -> ContentType.DATE
            content != null && content.length > 512 -> ContentType.PIC
            else -> ContentType.TEXT
        }
    }

    fun formatName(attrKey: String): String {
        val removedSuffix = attrKey.removeSuffix("_ms")
        return removedSuffix.replace('_', ' ')
    }

    fun formatValueText(attrKey: String,
                        attrValue: Any?,
                        maxWidth: Int = Int.MAX_VALUE,
                        maxWidthWithKey: Int = Int.MAX_VALUE): String
    {
        val str = attrValue?.toString()

        val keyStr = formatName(attrKey)
        val maxTextWidth = min(maxWidth, maxWidthWithKey - keyStr.length)

        when(contentType(attrKey, attrValue)) {
            ContentType.TEXT -> {
                str ?: return "---"
                return str.abbreviate(maxTextWidth, ending = "..")
            }

            ContentType.DATE -> {
                str ?: return "--/--/--"
                val date = Date(str.toLong())
                return DateFormat.getDateInstance().format(date)
            }

            ContentType.PIC -> return "<pic>"
        }
    }

}

fun String.abbreviate(maxWidth: Int, ending: String = "..."): String {
    return if (length <= maxWidth)
        return this
    else
        this.take(maxWidth - ending.length) + ending
}