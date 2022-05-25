package io.github.siyual_park.data.repository.cache

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.repository.TransactionalQueryRepositoryTestHelper
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepositoryBuilder
import io.github.siyual_park.data.repository.r2dbc.migration.CreatePerson
import io.github.siyual_park.ulid.ULID
import java.time.Duration

class CachedQueryRepositoryTest : TransactionalQueryRepositoryTestHelper(
    repositories = {
        listOf(
            R2DBCRepositoryBuilder<Person, ULID>(it.entityOperations, Person::class)
                .enableCache {
                    CacheBuilder.newBuilder()
                        .softValues()
                        .expireAfterAccess(Duration.ofMinutes(2))
                        .expireAfterWrite(Duration.ofMinutes(5))
                        .maximumSize(1_000)
                }.build()
        )
    }
) {
    init {
        migrationManager.register(CreatePerson(entityOperations))
    }
}
