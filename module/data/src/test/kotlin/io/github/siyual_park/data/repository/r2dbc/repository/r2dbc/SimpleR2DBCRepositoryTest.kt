package io.github.siyual_park.data.repository.r2dbc.repository.r2dbc

import io.github.siyual_park.data.repository.r2dbc.SimpleR2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.entity.Person

class SimpleR2DBCRepositoryTest : R2DBCRepositoryTestHelper(
    repositories = { listOf(SimpleR2DBCRepository(it.entityOperations, Person::class)) }
)
