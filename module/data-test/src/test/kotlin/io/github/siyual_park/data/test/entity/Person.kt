package io.github.siyual_park.data.test.entity

import io.github.siyual_park.data.AutoModifiable
import io.github.siyual_park.data.Modifiable
import io.github.siyual_park.data.ULIDEntity
import io.github.siyual_park.data.annotation.Key
import org.springframework.data.relational.core.mapping.Table

@Table("persons")
data class Person(
    @Key
    var name: String,
    var age: Int
) : ULIDEntity(), Modifiable by AutoModifiable()
