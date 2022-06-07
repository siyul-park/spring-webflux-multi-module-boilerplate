package io.github.siyual_park.data.cache

import io.github.siyual_park.data.WeekProperty
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.test.RedisTestHelper
import io.github.siyual_park.ulid.ULID
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.redisson.api.RedissonReactiveClient
import java.time.Duration

class RedisStorageTest : StorageTestHelper(
    run {
        val idProperty = object : WeekProperty<Person, ULID?> {
            override fun get(entity: Person): ULID {
                return entity.id
            }
        }
        RedisStorage(
            redisClient,
            RedisStorage.Configuration(
                name = "test",
                ttl = Duration.ofMinutes(30),
                size = 1000
            ),
            idProperty
        )
    }
) {
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
