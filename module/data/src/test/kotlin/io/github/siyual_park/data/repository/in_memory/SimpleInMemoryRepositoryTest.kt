package io.github.siyual_park.data.repository.in_memory

import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.repository.RepositoryTestHelper
import io.github.siyual_park.ulid.ULID

class SimpleInMemoryRepositoryTest : RepositoryTestHelper<InMemoryRepository<Person, ULID>>(
    repositories = {
        listOf(
            SimpleInMemoryRepository(Person::class)
        )
    }
)
