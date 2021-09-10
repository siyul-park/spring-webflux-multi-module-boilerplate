package io.github.siyual_park.data.mock

import io.github.siyual_park.data.BaseEntity
import io.github.siyual_park.data.copyDefaultColumn
import org.springframework.data.relational.core.mapping.Table

@Table("persons")
data class Person(
    var name: String,
    var age: Int
) : BaseEntity<Person>() {
    override fun clone(): Person {
        return copyDefaultColumn(this.copy())
    }
}
