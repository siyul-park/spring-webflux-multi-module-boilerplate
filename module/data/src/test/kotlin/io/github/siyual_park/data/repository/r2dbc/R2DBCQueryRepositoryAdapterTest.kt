package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.repository.TransactionalQueryRepositoryTestHelper
import io.github.siyual_park.data.repository.r2dbc.migration.CreatePerson

class R2DBCQueryRepositoryAdapterTest : TransactionalQueryRepositoryTestHelper(
    repositories = {
        listOf(R2DBCQueryRepositoryAdapter(SimpleR2DBCRepository(EntityManager(it.entityOperations, Person::class))))
    }
) {
    init {
        migrationManager.register(CreatePerson(entityOperations))
    }
}
