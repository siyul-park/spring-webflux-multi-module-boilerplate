package io.github.siyual_park.data.repository.mongo

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.ulid.ULID
import java.time.Duration

class CachedMongoRepositoryTest : MongoRepositoryTestHelper(
    repositories = {
        listOf(
            MongoRepositoryBuilder<Person, ULID>(mongoTemplate, Person::class).set(
                CacheBuilder.newBuilder()
                    .softValues()
                    .expireAfterAccess(Duration.ofMinutes(2))
                    .expireAfterWrite(Duration.ofMinutes(5))
                    .maximumSize(1_000)
            )
                .build()
        )
    }
)
