package io.github.siyual_park.data.cache

import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.data.WeekProperty
import io.github.siyual_park.data.dummy.DummyPerson
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.ulid.ULID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class StorageTestHelper(
    private val storage: Storage<ULID, Person>
) : CoroutineTestHelper() {
    protected val nameIndex = object : WeekProperty<Person, String> {
        override fun get(entity: Person): String {
            return entity.name
        }
    }

    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            storage.clear()
        }
    }

    @Test
    fun createIndex() = blocking {
        assertFalse(storage.containsIndex("name"))
        storage.createIndex("name", nameIndex)
        assertTrue(storage.containsIndex("name"))
    }

    @Test
    fun removeIndex() = blocking {
        storage.createIndex("name", nameIndex)
        storage.removeIndex("name")
        assertFalse(storage.containsIndex("name"))
    }

    @Test
    fun containsIndex() = blocking {
        assertFalse(storage.containsIndex("name"))
        storage.createIndex("name", nameIndex)
        assertTrue(storage.containsIndex("name"))
    }

    @Test
    fun getIndexes() = blocking {
        assertEquals(emptyMap<String, WeekProperty<Person, *>>(), storage.getIndexes())
        storage.createIndex("name", nameIndex)
        assertEquals(mapOf("name" to nameIndex), storage.getIndexes())
    }

    @Test
    fun getIfPresent() = blocking {
        val value = DummyPerson.create()

        storage.createIndex("name", nameIndex)

        assertNull(storage.getIfPresent(value.id))
        assertNull(storage.getIfPresent("name", value.name))

        storage.add(value)

        assertEquals(value, storage.getIfPresent(value.id))
        assertEquals(value, storage.getIfPresent("name", value.name))

        storage.remove(value.id)
        assertEquals(value, storage.getIfPresent(value.id) { value })
        storage.remove(value.id)
        assertEquals(value, storage.getIfPresent("name", value.name) { value })
    }

    @Test
    fun remove() = blocking {
        val value = DummyPerson.create()

        storage.createIndex("name", nameIndex)

        storage.add(value)
        storage.remove(value.id)

        assertNull(storage.getIfPresent(value.id))
        assertNull(storage.getIfPresent("name", value.name))
    }

    @Test
    fun delete() = blocking {
        val value = DummyPerson.create()

        storage.createIndex("name", nameIndex)

        storage.add(value)
        storage.remove(value.id)

        assertNull(storage.getIfPresent(value.id))
        assertNull(storage.getIfPresent("name", value.name))
    }

    @Test
    fun put() = blocking {
        val value = DummyPerson.create()

        storage.createIndex("name", nameIndex)

        storage.add(value)

        assertEquals(value, storage.getIfPresent(value.id))
        assertEquals(value, storage.getIfPresent("name", value.name))
    }

    @Test
    fun entries() = blocking {
        val value = DummyPerson.create()

        storage.createIndex("name", nameIndex)

        assertEquals(emptySet<Pair<ULID, Person>>(), storage.entries())

        storage.add(value)

        assertEquals(setOf(value.id to value), storage.entries())
    }

    @Test
    fun clear() = blocking {
        val value = DummyPerson.create()

        storage.createIndex("name", nameIndex)

        storage.add(value)
        storage.clear()

        assertEquals(emptySet<Pair<ULID, Person>>(), storage.entries())

        assertNull(storage.getIfPresent(value.id))
        assertNull(storage.getIfPresent("name", value.name))
    }
}
