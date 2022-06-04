package io.github.siyual_park.data.cache

import com.github.javafaker.Faker
import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.util.username
import org.apache.commons.collections4.map.AbstractReferenceMap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PoolTest : CoroutineTestHelper() {
    private val faker = Faker()

    @Test
    fun pop() = blocking {
        val pool = Pool(AbstractReferenceMap.ReferenceStrength.HARD) { faker.name().username(10) }
        val value1 = faker.name().username(10)
        val value2 = faker.name().username(10)

        pool.push(value1)
        assertEquals(value1, pool.pop())

        pool.push(value2)
        assertEquals(value2, pool.pop())

        val other = pool.pop()
        assertNotEquals(value1, other)
        assertNotEquals(value2, other)
    }

    @Test
    fun push() = blocking {
        val pool = Pool(AbstractReferenceMap.ReferenceStrength.HARD) { faker.name().username(10) }
        val value = faker.name().username(10)

        assertTrue(pool.push(value))
        assertFalse(pool.push(value))
    }
}
