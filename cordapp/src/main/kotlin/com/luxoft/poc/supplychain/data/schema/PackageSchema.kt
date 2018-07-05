package com.luxoft.poc.supplychain.data.schema

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

import com.luxoft.poc.supplychain.data.state.Package
import javax.persistence.Lob

object PackageSchema

object PackageSchemaV1 : MappedSchema(
        version = 1,
        schemaFamily = PackageSchema.javaClass,
        mappedTypes = listOf(PersistentPackage::class.java)) {

    @Entity
    @Table(name = "package")
    class PersistentPackage(

            @Column(name = "serial")
            var serial: String,

            @Column(name = "owner")
            @Lob
            var owner: ByteArray,

            @Column(name = "state")
            var state: Int,

            @Column
            var patientDid: String

    ) : PersistentState() {
        constructor(product: Package): this(
                product.info.serial,
                product.owner.owningKey.encoded,
                product.info.state.ordinal,
                product.info.patientDid)
        constructor(): this("", ByteArray(0), -1, "")
    }
}