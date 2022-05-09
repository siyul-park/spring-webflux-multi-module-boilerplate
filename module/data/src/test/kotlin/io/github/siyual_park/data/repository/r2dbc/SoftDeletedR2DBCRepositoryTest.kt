package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.ulid.ULID

class SoftDeletedR2DBCRepositoryTest : R2DBCRepositoryTestHelper(
    repositories = {
        listOf(
            R2DBCRepositoryBuilder<Person, ULID>(it.entityOperations, Person::class)
                .softDelete(true)
                .build()
        )
    }
)
