package io.github.siyual_park.data.lock

import com.github.javafaker.Faker
import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

abstract class LockerTestHelper(
    private val locker: Locker
) : CoroutineTestHelper() {
    private val faker = Faker()

    @Test
    fun get() = blocking {
        val key = faker.random().hex()

        val lock1 = locker.get(key)
        val lock2 = locker.get(key)
        assertEquals(lock1, lock2)
    }

    @Test
    fun remove() = blocking {
        val key = faker.random().hex()

        val lock1 = locker.get(key)
        locker.remove(key)

        val lock2 = locker.get(key)
        assertNotEquals(lock1, lock2)
    }
}
