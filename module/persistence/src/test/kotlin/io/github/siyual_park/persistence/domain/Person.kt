package io.github.siyual_park.persistence.domain

import io.github.siyual_park.data.repository.Repository
import io.github.siyual_park.persistence.Persistence
import io.github.siyual_park.persistence.entity.PersonData
import io.github.siyual_park.persistence.proxy

class Person(
    value: PersonData,
    repository: Repository<PersonData, Long>
) : Persistence<PersonData, Long>(value, repository) {
    val id by proxy(root, PersonData::id)
    var name by proxy(root, PersonData::name)
    var age by proxy(root, PersonData::age)
}
