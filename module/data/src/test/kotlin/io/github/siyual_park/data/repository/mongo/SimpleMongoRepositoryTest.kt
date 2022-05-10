package io.github.siyual_park.data.repository.mongo

import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.ulid.ULID

class SimpleMongoRepositoryTest : MongoRepositoryTestHelper(
    repositories = {
        listOf(
            MongoRepositoryBuilder<Person, ULID>(mongoTemplate, Person::class)
                .build()
        )
    }
)
