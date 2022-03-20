package io.github.siyual_park.persistency.domain

import io.github.siyual_park.data.repository.Repository
import io.github.siyual_park.persistency.Aggregate
import io.github.siyual_park.persistency.entity.Person

class PersonAggregate(
    root: Person,
    repository: Repository<Person, Long>
) : Aggregate<Person, Long>(root, repository) {
    val id: Long?
        get() = root[Person::id]

    var name: String
        get() = root[Person::name]
        set(value) { root[Person::name] = value }

    var age: Int
        get() = root[Person::age]
        set(value) { root[Person::age] = value }
}
