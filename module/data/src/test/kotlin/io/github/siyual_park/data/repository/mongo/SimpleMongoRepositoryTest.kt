package io.github.siyual_park.data.repository.mongo

import io.github.siyual_park.data.entity.Person

class SimpleMongoRepositoryTest : MongoRepositoryTestHelper(
    repositories = {
        listOf(SimpleMongoRepository(mongoTemplate, Person::class))
    }
)
