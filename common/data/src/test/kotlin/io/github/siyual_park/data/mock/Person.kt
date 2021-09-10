package io.github.siyual_park.data.mock

import io.github.siyual_park.data.Cloneable
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("persons")
data class Person(
    var name: String,
    var age: Int,

    @Id var id: Long? = null,
    @Column("created_at") var createdAt: Instant? = null
) : Cloneable<Person> {
    override fun clone(): Person {
        return this.copy()
    }
}
