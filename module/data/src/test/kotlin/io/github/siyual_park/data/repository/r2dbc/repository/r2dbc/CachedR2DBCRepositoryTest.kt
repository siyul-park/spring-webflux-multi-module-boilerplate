package io.github.siyual_park.data.repository.r2dbc.repository.r2dbc

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepositoryBuilder
import io.github.siyual_park.data.repository.r2dbc.entity.Person
import io.github.siyual_park.ulid.ULID
import java.time.Duration

class CachedR2DBCRepositoryTest : R2DBCRepositoryTestHelper(
    repositories = {
        listOf(
            R2DBCRepositoryBuilder<Person, ULID>(it.entityOperations, Person::class)
                .set(
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
