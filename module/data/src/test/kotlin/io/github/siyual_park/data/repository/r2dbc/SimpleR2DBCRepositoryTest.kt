package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.entity.Person

class SimpleR2DBCRepositoryTest : R2DBCRepositoryTestHelper(
    repositories = { listOf(SimpleR2DBCRepository(EntityManager(it.entityOperations, Person::class))) }
)
