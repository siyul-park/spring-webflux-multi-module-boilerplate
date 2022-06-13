package io.github.siyual_park.data.cache

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.test.RedisTestHelper
import io.github.siyual_park.ulid.ULID
import io.github.siyual_park.ulid.jackson.ULIDModule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.redisson.api.RedissonClient
import java.time.Duration
import java.time.Instant

class RedisStorageTest : StorageTestHelper(
    run {
        val objectMapper = jacksonObjectMapper().apply {
            registerModule(ULIDModule())
        }

        RedisStorage(
            redisClient,
            name = "test",
            size = 1000,
            objectMapper = objectMapper,
            id = { it.id },
            expiredAt = { Instant.now().plus(Duration.ofMinutes(30)) },
            keyClass = ULID::class,
            valueClass = Person::class,
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
