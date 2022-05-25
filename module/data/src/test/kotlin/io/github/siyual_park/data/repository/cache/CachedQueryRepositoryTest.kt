package io.github.siyual_park.data.repository.cache

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.WeekProperty
import io.github.siyual_park.data.cache.InMemoryStorage
import io.github.siyual_park.data.cache.Pool
import io.github.siyual_park.data.cache.PoolingNestedStorage
import io.github.siyual_park.data.cache.TransactionalStorage
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.repository.TransactionalQueryRepositoryTestHelper
import io.github.siyual_park.data.repository.r2dbc.EntityManager
import io.github.siyual_park.data.repository.r2dbc.R2DBCQueryRepositoryAdapter
import io.github.siyual_park.data.repository.r2dbc.SimpleR2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.migration.CreatePerson
import io.github.siyual_park.ulid.ULID
import java.time.Duration

class CachedQueryRepositoryTest : TransactionalQueryRepositoryTestHelper(
    repositories = {
        val id = object : WeekProperty<Person, ULID> {
            override fun get(entity: Person): ULID {
                return entity.id
            }
        }

        listOf(
            CachedQueryRepository(
                R2DBCQueryRepositoryAdapter(SimpleR2DBCRepository(EntityManager(it.entityOperations, Person::class))),
                TransactionalStorage(
                    PoolingNestedStorage(
                        Pool {
                            InMemoryStorage({
                                CacheBuilder.newBuilder()
                                    .softValues()
                                    .expireAfterAccess(Duration.ofMinutes(2))
                                    .expireAfterWrite(Duration.ofMinutes(5))
                                    .maximumSize(1_000)
                            }, id)
                        },
                        id
                    )
                ),
                id,
                Person::class
            )
        )
    }
) {
    init {
        migrationManager.register(CreatePerson(entityOperations))
    }
}
