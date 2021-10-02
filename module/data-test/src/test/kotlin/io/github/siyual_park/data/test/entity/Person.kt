package io.github.siyual_park.data.test.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.copyDefaultColumn
import org.springframework.data.relational.core.mapping.Table

@Table("persons")
data class Person(
    var name: String,
    var age: Int
) : TimeableEntity<io.github.siyual_park.data.test.entity.Person, Long>() {
    override fun clone(): io.github.siyual_park.data.test.entity.Person {
        return copyDefaultColumn(this.copy())
    }
}
