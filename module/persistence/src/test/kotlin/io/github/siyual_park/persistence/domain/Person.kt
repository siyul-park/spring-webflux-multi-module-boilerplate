package io.github.siyual_park.persistence.domain

import io.github.siyual_park.data.repository.Repository
import io.github.siyual_park.persistence.Persistence
import io.github.siyual_park.persistence.entity.PersonData

class Person(
    root: PersonData,
    repository: Repository<PersonData, Long>
) : Persistence<PersonData, Long>(root, repository) {
    val id: Long?
        get() = root[PersonData::id]

    var name: String
        get() = root[PersonData::name]
        set(value) { root[PersonData::name] = value }

    var age: Int
        get() = root[PersonData::age]
        set(value) { root[PersonData::age] = value }
}
