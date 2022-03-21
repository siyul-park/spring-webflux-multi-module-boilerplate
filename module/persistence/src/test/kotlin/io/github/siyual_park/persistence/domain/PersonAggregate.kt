package io.github.siyual_park.persistence.domain

import io.github.siyual_park.data.repository.Repository
import io.github.siyual_park.persistence.Persistence
import io.github.siyual_park.persistence.entity.Person

class PersonAggregate(
    root: Person,
    repository: Repository<Person, Long>
) : Persistence<Person, Long>(root, repository) {
    val id: Long?
        get() = root[Person::id]

    var name: String
        get() = root[Person::name]
        set(value) { root[Person::name] = value }

    var age: Int
        get() = root[Person::age]
        set(value) { root[Person::age] = value }
}
