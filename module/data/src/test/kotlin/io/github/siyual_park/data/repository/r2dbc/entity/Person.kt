package io.github.siyual_park.data.repository.r2dbc.entity

import io.github.siyual_park.data.ModifiableULIDEntity
import io.github.siyual_park.data.SoftDeletable
import io.github.siyual_park.data.annotation.Key
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("persons")
@Document("persons")
data class Person(
    @Key
    var name: String,
    var age: Int,
    override var deletedAt: Instant? = null
) : ModifiableULIDEntity(), SoftDeletable
