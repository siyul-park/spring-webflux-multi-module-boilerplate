package io.github.siyual_park.data.cache

import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.test.DummyStringFactory
import org.apache.commons.collections4.map.AbstractReferenceMap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PoolTest : CoroutineTestHelper() {
    @Test
    fun pop() = blocking {
        val pool = Pool(AbstractReferenceMap.ReferenceStrength.HARD) { DummyStringFactory.create(10) }
        val value1 = DummyStringFactory.create(10)
        val value2 = DummyStringFactory.create(10)

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
        val pool = Pool(AbstractReferenceMap.ReferenceStrength.HARD) { DummyStringFactory.create(10) }
        val value = DummyStringFactory.create(10)

        assertTrue(pool.push(value))
        assertFalse(pool.push(value))
    }
}
