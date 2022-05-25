package io.github.siyual_park.data.cache

import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.test.DummyStringFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class QueryStorageTestHelper(private val storage: QueryStorage<String>) : CoroutineTestHelper() {
    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            storage.clear()
        }
    }

    @Test
    fun getIfPresent() = blocking {
        val key = DummyStringFactory.create(10)
        val value = DummyStringFactory.create(10)

        assertNull(storage.getIfPresent(key))
        assertEquals(value, storage.getIfPresent(key) { value })
        assertNull(storage.getIfPresent(SelectQuery(key)))
        assertEquals(listOf(value), storage.getIfPresent(SelectQuery(key)) { listOf(value) })
    }

    @Test
    fun remove() = blocking {
        val key = DummyStringFactory.create(10)
        val value = DummyStringFactory.create(10)

        storage.put(SelectQuery(key), listOf(value))

        storage.remove(SelectQuery(key))

        assertNull(storage.getIfPresent(SelectQuery(key)))
    }

    @Test
    fun put() = blocking {
        val key = DummyStringFactory.create(10)
        val value = DummyStringFactory.create(10)

        storage.put(SelectQuery(key), listOf(value))

        assertEquals(listOf(value), storage.getIfPresent(SelectQuery(key)))
    }

    @Test
    fun clear() = blocking {
        val key = DummyStringFactory.create(10)
        val value = DummyStringFactory.create(10)

        storage.put(SelectQuery(key), listOf(value))

        storage.clear()

        assertNull(storage.getIfPresent(SelectQuery(key)))
    }

    @Test
    fun entries() = blocking {
        val key = DummyStringFactory.create(10)
        val value = DummyStringFactory.create(10)

        storage.put(SelectQuery(key), listOf(value))

        val multi = storage.entries()

        assertEquals(setOf(SelectQuery(key) to listOf(value)), multi)
    }
}
