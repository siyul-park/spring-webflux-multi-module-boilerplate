package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.repository.TransactionalQueryRepositoryTestHelper
import io.github.siyual_park.data.repository.r2dbc.migration.CreatePerson
import io.github.siyual_park.ulid.ULID

class R2DBCQueryRepositoryAdapterTest : TransactionalQueryRepositoryTestHelper(
    repositories = {
        listOf(R2DBCRepositoryBuilder<Person, ULID>(it.entityOperations, Person::class).build())
    }
) {
    init {
        migrationManager.register(CreatePerson(entityOperations))
    }
}
