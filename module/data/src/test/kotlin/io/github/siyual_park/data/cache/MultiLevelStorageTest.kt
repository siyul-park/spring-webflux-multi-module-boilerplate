package io.github.siyual_park.data.cache

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.data.WeekProperty
import io.github.siyual_park.data.dummy.DummyPerson
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.ulid.ULID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MultiLevelStorageTest : CoroutineTestHelper() {
    private val idProperty = object : WeekProperty<Person, ULID?> {
        override fun get(entity: Person): ULID {
            return entity.id
        }
    }
    private val storage1 = InMemoryStorage(
        { CacheBuilder.newBuilder() },
        idProperty
    )
    private val storage2 = InMemoryStorage(
        { CacheBuilder.newBuilder() },
        idProperty
    )
    private val storage = MultiLevelStorage(storage1)

    init {
        storage.register(storage2)
    }

    @Test
    fun index() = blocking {
        val nameIndex = object : WeekProperty<Person, String> {
            override fun get(entity: Person): String {
                return entity.name
            }
        }

        assertFalse(storage.containsIndex("name"))
        storage.createIndex("name", nameIndex)
        assertTrue(storage.containsIndex("name"))
        storage.removeIndex("name")
        assertFalse(storage.containsIndex("name"))
    }

    @Test
    fun getIndexes() = blocking {
        val indexes = storage.getIndexes()
        assertEquals(0, indexes.size)
    }

    @Test
    fun getIfPresent() = blocking {
        val nameIndex = object : WeekProperty<Person, String> {
            override fun get(entity: Person): String {
                return entity.name
            }
        }
        storage.createIndex("name", nameIndex)

        val person = DummyPerson.create()

        assertNull(storage.getIfPresent(person.id))
        storage.add(person)
        assertNotNull(storage.getIfPresent(person.id))

        storage.remove(person.id)
        assertNotNull(storage.getIfPresent(person.id) { person })
        storage.remove(person.id)

        assertNull(storage.getIfPresent("name", person.name))
        storage.add(person)
        assertNotNull(storage.getIfPresent("name", person.name))

        storage.remove(person.id)
        assertNotNull(storage.getIfPresent("name", person.name) { person })
        storage.remove(person.id)

        storage.removeIndex("name")
    }

    @Test
    fun remove() = blocking {
        val person = DummyPerson.create()

        storage.add(person)
        storage.remove(person.id)

        assertNull(storage.getIfPresent(person.id))
        assertNull(storage1.getIfPresent(person.id))
        assertNull(storage2.getIfPresent(person.id))
    }

    @Test
    fun put() = blocking {
        val person = DummyPerson.create()

        storage.add(person)
        assertNotNull(storage.getIfPresent(person.id))
        assertNotNull(storage1.getIfPresent(person.id))
        assertNotNull(storage2.getIfPresent(person.id))
    }

    @Test
    fun clear() = blocking {
        val person = DummyPerson.create()

        storage.add(person)
        storage.clear()

        assertNull(storage.getIfPresent(person.id))
        assertNull(storage1.getIfPresent(person.id))
        assertNull(storage2.getIfPresent(person.id))
    }
}
