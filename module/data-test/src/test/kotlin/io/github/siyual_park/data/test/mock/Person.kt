package io.github.siyual_park.data.test.mock

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.copyDefaultColumn
import org.springframework.data.relational.core.mapping.Table

@Table("persons")
data class Person(
    var name: String,
    var age: Int
) : TimeableEntity<Person, Long>() {
    override fun clone(): Person {
        return copyDefaultColumn(this.copy())
    }
}
