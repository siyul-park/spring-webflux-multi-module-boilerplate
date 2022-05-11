package io.github.siyual_park.data.repository.cache

import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.repository.RepositoryTestHelper
import io.github.siyual_park.data.repository.in_memory.InMemoryRepository
import io.github.siyual_park.data.repository.in_memory.SimpleInMemoryRepository
import io.github.siyual_park.ulid.ULID

class SimpleCacheRepositoryTest : RepositoryTestHelper<InMemoryRepository<Person, ULID>>(
    repositories = {
        listOf(
            SimpleInMemoryRepository(Person::class)
        )
    }
)
