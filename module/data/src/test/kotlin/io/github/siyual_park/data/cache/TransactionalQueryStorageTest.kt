package io.github.siyual_park.data.cache

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.test.DummyStringFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TransactionalQueryStorageTest : CoroutineTestHelper() {
    private val storage = TransactionalQueryStorage<String>(
        NestedQueryStorage(
            LoadingPool { InMemoryQueryStorage { CacheBuilder.newBuilder() } }
        )
    )

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

        Assertions.assertNull(storage.getIfPresent(key))
        Assertions.assertEquals(value, storage.getIfPresent(key) { value })
        Assertions.assertNull(storage.getIfPresent(SelectQuery(key)))
        Assertions.assertEquals(listOf(value), storage.getIfPresent(SelectQuery(key)) { listOf(value) })
    }

    @Test
    fun remove() = blocking {
        val key = DummyStringFactory.create(10)
        val value = DummyStringFactory.create(10)

        storage.put(key, value)
        storage.put(SelectQuery(key), listOf(value))

        storage.remove(key)
        storage.remove(SelectQuery(key))

        Assertions.assertNull(storage.getIfPresent(key))
        Assertions.assertNull(storage.getIfPresent(SelectQuery(key)))
    }

    @Test
    fun put() = blocking {
        val key = DummyStringFactory.create(10)
        val value = DummyStringFactory.create(10)

        storage.put(key, value)
        storage.put(SelectQuery(key), listOf(value))

        Assertions.assertEquals(value, storage.getIfPresent(key))
        Assertions.assertEquals(listOf(value), storage.getIfPresent(SelectQuery(key)))
    }

    @Test
    fun clear() = blocking {
        val key = DummyStringFactory.create(10)
        val value = DummyStringFactory.create(10)

        storage.put(key, value)
        storage.put(SelectQuery(key), listOf(value))

        storage.clear()

        Assertions.assertNull(storage.getIfPresent(key))
        Assertions.assertNull(storage.getIfPresent(SelectQuery(key)))
    }

    @Test
    fun entries() = blocking {
        val key = DummyStringFactory.create(10)
        val value = DummyStringFactory.create(10)

        storage.put(key, value)
        storage.put(SelectQuery(key), listOf(value))

        val (single, multi) = storage.entries()

        Assertions.assertEquals(setOf(key to value), single)
        Assertions.assertEquals(setOf(SelectQuery(key) to listOf(value)), multi)
    }
}
