package io.github.siyual_park.data.cache

import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.data.criteria.where
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
        assertNull(storage.getIfPresent(SelectQuery(where(key).`is`(value))))
        assertEquals(listOf(value), storage.getIfPresent(SelectQuery(where(key).`is`(value))) { listOf(value) })
    }

    @Test
    fun remove() = blocking {
        val key = DummyStringFactory.create(10)
        val value = DummyStringFactory.create(10)

        storage.put(SelectQuery(where(key).`is`(value)), listOf(value))

        storage.remove(SelectQuery(where(key).`is`(value)))

        assertNull(storage.getIfPresent(SelectQuery(where(key).`is`(value))))
    }

    @Test
    fun put() = blocking {
        val key = DummyStringFactory.create(10)
        val value = DummyStringFactory.create(10)

        storage.put(SelectQuery(where(key).`is`(value)), listOf(value))

        assertEquals(listOf(value), storage.getIfPresent(SelectQuery(where(key).`is`(value))))
    }

    @Test
    fun clear() = blocking {
        val key = DummyStringFactory.create(10)
        val value = DummyStringFactory.create(10)

        storage.put(SelectQuery(where(key).`is`(value)), listOf(value))

        storage.clear()

        assertNull(storage.getIfPresent(SelectQuery(where(key).`is`(value))))
    }

    @Test
    fun entries() = blocking {
        val key = DummyStringFactory.create(10)
        val value = DummyStringFactory.create(10)

        storage.put(SelectQuery(where(key).`is`(value)), listOf(value))

        val multi = storage.entries()

        assertEquals(setOf(SelectQuery(where(key).`is`(value)) to listOf(value)), multi)
    }
}
