package io.github.siyual_park.data.repository.cache

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.data.dummy.DummyPerson
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.repository.Extractor
import io.github.siyual_park.ulid.ULID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MultiLevelStorageTest : CoroutineTestHelper() {
    private val idExtractor = object : Extractor<Person, ULID> {
        override fun getKey(entity: Person): ULID {
            return entity.id
        }
    }
    private val storage1 = InMemoryStorage(
        { CacheBuilder.newBuilder() },
        idExtractor
    )
    private val storage2 = InMemoryStorage(
        { CacheBuilder.newBuilder() },
        idExtractor
    )
    private val storage = MultiLevelStorage(storage1)

    init {
        storage.register(storage2)
    }

    @Test
    fun index() = blocking {
        val nameIndex = object : Extractor<Person, String> {
            override fun getKey(entity: Person): String {
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
    fun getExtractors() = blocking {
        val extractors = storage.getExtractors()
        assertEquals(0, extractors.size)
    }

    @Test
    fun getIfPresent() = blocking {
        val nameIndex = object : Extractor<Person, String> {
            override fun getKey(entity: Person): String {
                return entity.name
            }
        }
        storage.createIndex("name", nameIndex)

        val person = DummyPerson.create()

        assertNull(storage.getIfPresent(person.id))
        storage.put(person)
        assertNotNull(storage.getIfPresent(person.id))

        storage.delete(person)
        assertNotNull(storage.getIfPresent(person.id) { person })
        storage.delete(person)

        assertNull(storage.getIfPresent("name", person.name))
        storage.put(person)
        assertNotNull(storage.getIfPresent("name", person.name))

        storage.delete(person)
        assertNotNull(storage.getIfPresent("name", person.name) { person })
        storage.delete(person)

        storage.removeIndex("name")
    }

    @Test
    fun remove() = blocking {
        val person = DummyPerson.create()

        storage.put(person)
        storage.remove(person.id)

        assertNull(storage.getIfPresent(person.id))
        assertNull(storage1.getIfPresent(person.id))
        assertNull(storage2.getIfPresent(person.id))
    }

    @Test
    fun delete() = blocking {
        val person = DummyPerson.create()

        storage.put(person)
        storage.delete(person)

        assertNull(storage.getIfPresent(person.id))
        assertNull(storage1.getIfPresent(person.id))
        assertNull(storage2.getIfPresent(person.id))
    }

    @Test
    fun put() = blocking {
        val person = DummyPerson.create()

        storage.put(person)
        assertNotNull(storage.getIfPresent(person.id))
        assertNotNull(storage1.getIfPresent(person.id))
        assertNotNull(storage2.getIfPresent(person.id))
    }

    @Test
    fun clear() = blocking {
        val person = DummyPerson.create()

        storage.put(person)
        storage.clear()

        assertNull(storage.getIfPresent(person.id))
        assertNull(storage1.getIfPresent(person.id))
        assertNull(storage2.getIfPresent(person.id))
    }
}
