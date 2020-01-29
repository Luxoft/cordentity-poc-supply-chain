package com.luxoft.supplychain.sovrinagentapp.data

object KnownSchemas {
    object PersonalId {
        val schemaName = "Patient Demographics"

        object attributes {
            val name = "Full_Legal_Name"
        }
    }

    object Common {
        object attributes {
            val type = "Credential_Type"
            val name = "Credential_Name"
            val issuer = "Credential_Issuer"
        }
    }
}