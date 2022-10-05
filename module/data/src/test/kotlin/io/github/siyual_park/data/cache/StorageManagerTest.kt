package io.github.siyual_park.data.cache

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.data.WeekProperty
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.ulid.ULID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StorageManagerTest : CoroutineTestHelper() {
    private val idProperty = object : WeekProperty<Person, ULID?> {
        override fun get(entity: Person): ULID {
            return entity.id
        }
    }

    @Test
    fun put() {
        val storageManager = StorageManager()
        val storage = InMemoryStorage(
            { CacheBuilder.newBuilder() },
            idProperty
        )

        val key = ULID.randomULID()
        storageManager.put(key.toString(), storage)

        assertEquals(storage, storageManager.get(key.toString()))
    }

    @Test
    fun remove() {
        val storageManager = StorageManager()
        val storage = InMemoryStorage(
            { CacheBuilder.newBuilder() },
            idProperty
        )

        val key = ULID.randomULID()

        assertEquals(null, storageManager.remove(key.toString()))

        storageManager.put(key.toString(), storage)
        assertEquals(storage, storageManager.remove(key.toString()))
    }

    @Test
    fun status() = blocking {
        val storageManager = StorageManager()
        val storage = InMemoryStorage(
            { CacheBuilder.newBuilder() },
            idProperty
        )

        val key = ULID.randomULID()

        storageManager.put(key.toString(), storage)
        assertEquals(mapOf(key.toString() to storage.status()), storageManager.status())
    }
}
