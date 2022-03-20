package io.github.siyual_park.persistence.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.annotation.Key
import org.springframework.data.relational.core.mapping.Table

@Table("persons")
data class Person(
    @Key
    var name: String,
    var age: Int
) : TimeableEntity<Person, Long>()
