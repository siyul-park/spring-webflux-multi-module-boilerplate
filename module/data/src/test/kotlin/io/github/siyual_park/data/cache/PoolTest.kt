package io.github.siyual_park.data.cache

import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.test.DummyStringFactory
import org.apache.commons.collections4.map.AbstractReferenceMap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PoolTest : CoroutineTestHelper() {
    @Test
    fun add() = blocking {
        val pool = Pool<String>(AbstractReferenceMap.ReferenceStrength.HARD)
        val value = DummyStringFactory.create(10)

        assertTrue(pool.add(value))
        assertFalse(pool.add(value))
    }

    @Test
    fun remove() = blocking {
        val pool = Pool<String>(AbstractReferenceMap.ReferenceStrength.HARD)
        val value = DummyStringFactory.create(10)

        assertTrue(pool.add(value))
        assertTrue(pool.remove(value))
        assertFalse(pool.remove(value))
    }

    @Test
    fun poll() = blocking {
        val pool = Pool<String>(AbstractReferenceMap.ReferenceStrength.HARD)
        val value = DummyStringFactory.create(10)

        assertNull(pool.poll())
        assertTrue(pool.add(value))
        assertEquals(value, pool.poll())
        assertNull(pool.poll())
    }
}
