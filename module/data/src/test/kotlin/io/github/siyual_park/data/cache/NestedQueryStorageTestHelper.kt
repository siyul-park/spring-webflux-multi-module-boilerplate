package io.github.siyual_park.data.cache

import io.github.siyual_park.test.DummyStringFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

abstract class NestedQueryStorageTestHelper(private val storage: NestedQueryStorage<String>) : QueryStorageTestHelper(storage) {
    @Test
    fun putInNested() = blocking {
        val child1 = storage.fork()
        val child2 = child1.fork()

        val key = DummyStringFactory.create(10)
        val value = DummyStringFactory.create(10)

        child2.put(key, value)
        child2.put(SelectQuery(key), listOf(value))

        assertNull(storage.getIfPresent(key))
        assertNull(storage.getIfPresent(SelectQuery(key)))
        assertNull(child1.getIfPresent(key))
        assertNull(child1.getIfPresent(SelectQuery(key)))
        assertEquals(value, child2.getIfPresent(key))
        assertEquals(listOf(value), child2.getIfPresent(SelectQuery(key)))

        child1.put(key, value)
        child1.put(SelectQuery(key), listOf(value))

        assertNull(storage.getIfPresent(key))
        assertNull(storage.getIfPresent(SelectQuery(key)))
        assertEquals(value, child1.getIfPresent(key))
        assertEquals(listOf(value), child1.getIfPresent(SelectQuery(key)))
        assertEquals(value, child2.getIfPresent(key))
        assertEquals(listOf(value), child2.getIfPresent(SelectQuery(key)))

        storage.put(key, value)
        storage.put(SelectQuery(key), listOf(value))

        assertEquals(value, storage.getIfPresent(key))
        assertEquals(listOf(value), storage.getIfPresent(SelectQuery(key)))
        assertEquals(value, child1.getIfPresent(key))
        assertEquals(listOf(value), child1.getIfPresent(SelectQuery(key)))
        assertEquals(value, child2.getIfPresent(key))
        assertEquals(listOf(value), child2.getIfPresent(SelectQuery(key)))
    }

    @Test
    fun clearInNested() = blocking {
        val child1 = storage.fork()
        val child2 = child1.fork()

        val key = DummyStringFactory.create(10)
        val value = DummyStringFactory.create(10)

        child2.put(key, value)
        child2.put(SelectQuery(key), listOf(value))

        child1.clear()

        assertNull(storage.getIfPresent(key))
        assertNull(storage.getIfPresent(SelectQuery(key)))
        assertNull(child1.getIfPresent(key))
        assertNull(child1.getIfPresent(SelectQuery(key)))
        assertNull(child2.getIfPresent(key))
        assertNull(child2.getIfPresent(SelectQuery(key)))

        child1.put(key, value)
        child1.put(SelectQuery(key), listOf(value))

        storage.clear()

        assertNull(storage.getIfPresent(key))
        assertNull(storage.getIfPresent(SelectQuery(key)))
        assertNull(child1.getIfPresent(key))
        assertNull(child1.getIfPresent(SelectQuery(key)))
        assertNull(child2.getIfPresent(key))
        assertNull(child2.getIfPresent(SelectQuery(key)))

        storage.put(key, value)
        storage.put(SelectQuery(key), listOf(value))

        child2.clear()

        assertNull(storage.getIfPresent(key))
        assertNull(storage.getIfPresent(SelectQuery(key)))
        assertNull(child1.getIfPresent(key))
        assertNull(child1.getIfPresent(SelectQuery(key)))
        assertNull(child2.getIfPresent(key))
        assertNull(child2.getIfPresent(SelectQuery(key)))
    }

    @Test
    fun merge() = blocking {
        val child1 = storage.fork()
        val child2 = child1.fork()

        val key = DummyStringFactory.create(10)
        val value = DummyStringFactory.create(10)

        child2.put(key, value)
        child2.put(SelectQuery(key), listOf(value))

        assertNull(storage.getIfPresent(key))
        assertNull(storage.getIfPresent(SelectQuery(key)))
        assertNull(child1.getIfPresent(key))
        assertNull(child1.getIfPresent(SelectQuery(key)))
        assertEquals(value, child2.getIfPresent(key))
        assertEquals(listOf(value), child2.getIfPresent(SelectQuery(key)))

        child1.merge(child2)

        assertNull(storage.getIfPresent(key))
        assertNull(storage.getIfPresent(SelectQuery(key)))
        assertEquals(value, child1.getIfPresent(key))
        assertEquals(listOf(value), child1.getIfPresent(SelectQuery(key)))
        assertEquals(value, child2.getIfPresent(key))
        assertEquals(listOf(value), child2.getIfPresent(SelectQuery(key)))

        storage.merge(child1)

        assertEquals(value, storage.getIfPresent(key))
        assertEquals(listOf(value), storage.getIfPresent(SelectQuery(key)))
        assertEquals(value, child1.getIfPresent(key))
        assertEquals(listOf(value), child1.getIfPresent(SelectQuery(key)))
        assertEquals(value, child2.getIfPresent(key))
        assertEquals(listOf(value), child2.getIfPresent(SelectQuery(key)))
    }

    @Test
    fun fork() = blocking {
        val child1 = storage.fork()
        val child2 = child1.fork()

        val key = DummyStringFactory.create(10)
        val value = DummyStringFactory.create(10)

        storage.put(key, value)
        storage.put(SelectQuery(key), listOf(value))

        assertEquals(value, storage.getIfPresent(key))
        assertEquals(listOf(value), storage.getIfPresent(SelectQuery(key)))
        assertEquals(value, child1.getIfPresent(key))
        assertEquals(listOf(value), child1.getIfPresent(SelectQuery(key)))
        assertEquals(value, child2.getIfPresent(key))
        assertEquals(listOf(value), child2.getIfPresent(SelectQuery(key)))
    }
}
