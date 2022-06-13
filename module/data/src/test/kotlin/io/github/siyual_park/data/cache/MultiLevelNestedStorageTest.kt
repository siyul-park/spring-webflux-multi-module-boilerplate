package io.github.siyual_park.data.cache

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.WeekProperty
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.test.RedisTestHelper
import io.github.siyual_park.ulid.ULID
import io.github.siyual_park.ulid.jackson.ULIDModule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.redisson.api.RedissonClient
import java.time.Duration
import java.time.Instant

class MultiLevelNestedStorageTest : NestedStorageTestHelper(
    run {
        val idProperty = WeekProperty<Person, ULID?> { entity -> entity.id }
        val objectMapper = jacksonObjectMapper().apply {
            registerModule(ULIDModule())
        }

        MultiLevelNestedStorage(
            RedisStorage(
                redisClient,
                name = "test",
                size = 1000,
                objectMapper = objectMapper,
                id = idProperty,
                expiredAt = { Instant.now().plus(Duration.ofMinutes(30)) },
                keyClass = ULID::class,
                valueClass = Person::class,
            ),
            Pool {
                InMemoryStorage(
                    { CacheBuilder.newBuilder() },
                    idProperty
                )
            },
            idProperty
        )
    }
) {
    companion object {
        private val helper = RedisTestHelper()

        val redisClient: RedissonClient
            get() = helper.redisClient

        @BeforeAll
        @JvmStatic
        fun setUpAll() = helper.setUp()

        @AfterAll
        @JvmStatic
        fun tearDownAll() = helper.tearDown()
    }
}
