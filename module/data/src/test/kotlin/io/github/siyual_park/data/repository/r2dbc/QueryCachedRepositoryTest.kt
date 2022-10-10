package io.github.siyual_park.data.repository.r2dbc

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.repository.TransactionalQueryableRepositoryTestHelper
import io.github.siyual_park.data.repository.r2dbc.migration.CreatePerson
import io.github.siyual_park.ulid.ULID
import java.time.Duration

class QueryCachedRepositoryTest : TransactionalQueryableRepositoryTestHelper(
    repositories = {
        listOf(
            R2DBCRepositoryBuilder<Person, ULID>(it.entityOperations, Person::class)
                .enableQueryCache {
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
