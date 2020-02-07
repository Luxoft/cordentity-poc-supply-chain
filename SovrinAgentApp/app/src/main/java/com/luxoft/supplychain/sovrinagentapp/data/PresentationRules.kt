package com.luxoft.supplychain.sovrinagentapp.data

import com.luxoft.blockchainlab.hyperledger.indy.models.CredentialReference
import java.lang.Integer.min
import java.text.DateFormat
import java.util.*

class CredentialPresentationRules() {

    fun formatName(cred: CredentialReference): String {
        return cred.attributes[KnownSchemas.Common.attributes.type]?.toString()
            ?: cred.getSchemaIdObject().name
    }

    fun formatDescription(cred: CredentialReference): String? {
        return cred.attributes[KnownSchemas.Common.attributes.name]?.toString()
    }

    fun formatIssuerName(cred: CredentialReference): String? {
        return cred.attributes[KnownSchemas.Common.attributes.issuer]?.toString()
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
        return removedSuffix.replace('_', ' ').trim()
    }

    fun formatValueText(attrKey: String,
                        attrValue: Any?,
                        maxWidth: Int = Int.MAX_VALUE,
                        maxWidthWithKey: Int = Int.MAX_VALUE): String
    {
        val str = attrValue?.toString()?.trim()

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
    return when {
        length <= maxWidth -> return this
        maxWidth < ending.length -> "?"
        else -> this.take(maxWidth - ending.length).trimEnd() + ending
    }
}