package io.github.siyual_park.data.repository.mongo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.jackson.instant.InstantEpochTimeModule
import io.github.siyual_park.data.repository.TransactionalQueryRepositoryTestHelper
import io.github.siyual_park.data.repository.mongo.migration.CreatePerson
import io.github.siyual_park.data.test.MongoTestHelper
import io.github.siyual_park.data.test.RedisTestHelper
import io.github.siyual_park.ulid.ULID
import io.github.siyual_park.ulid.jackson.ULIDModule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.redisson.api.RedissonReactiveClient
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import java.time.Duration

class RedisCacheRepositoryTest : TransactionalQueryRepositoryTestHelper(
    repositories = {
        listOf(
            MongoRepositoryBuilder<Person, ULID>(mongoTemplate, Person::class)
                .enableJsonMapping(
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
        migrationManager.register(CreatePerson(mongoTemplate))
    }

    companion object {
        private val redisHelper = RedisTestHelper()
        private val mongoHelper = MongoTestHelper()

        val redisClient: RedissonReactiveClient
            get() = redisHelper.redisClient
        val mongoTemplate: ReactiveMongoTemplate
            get() = mongoHelper.mongoTemplate

        @BeforeAll
        @JvmStatic
        fun setUpAll() {
            redisHelper.setUp()
            mongoHelper.setUp()
        }

        @AfterAll
        @JvmStatic
        fun tearDownAll() {
            redisHelper.tearDown()
            mongoHelper.tearDown()
        }
    }
}
