package io.github.siyual_park.data.repository.r2dbc

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.jackson.instant.InstantEpochTimeModule
import io.github.siyual_park.data.repository.TransactionalQueryRepositoryTestHelper
import io.github.siyual_park.data.repository.r2dbc.migration.CreatePerson
import io.github.siyual_park.data.test.RedisTestHelper
import io.github.siyual_park.ulid.ULID
import io.github.siyual_park.ulid.jackson.ULIDModule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.redisson.api.RedissonReactiveClient
import java.time.Duration

class RedisCachedQueryRepositoryTest : TransactionalQueryRepositoryTestHelper(
    repositories = {
        listOf(
            R2DBCRepositoryBuilder<Person, ULID>(it.entityOperations, Person::class)
                .setObjectMapper(
                    jacksonObjectMapper().apply {
                        registerModule(ULIDModule())
                        registerModule(InstantEpochTimeModule())
                    }
                )
                .enableCache(redisClient, ttl = Duration.ofMinutes(10), size = 1000)
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

    companion object {
        private val helper = RedisTestHelper()

        val redisClient: RedissonReactiveClient
            get() = helper.redisClient

        @BeforeAll
        @JvmStatic
        fun setUpAll() = helper.setUp()

        @AfterAll
        @JvmStatic
        fun tearDownAll() = helper.tearDown()
    }
}
