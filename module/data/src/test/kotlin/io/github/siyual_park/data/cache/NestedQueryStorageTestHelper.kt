package io.github.siyual_park.data.cache

import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.data.dummy.DummyPerson
import io.github.siyual_park.data.entity.Person
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

abstract class NestedQueryStorageTestHelper(private val storage: NestedQueryStorage<Person>) : QueryStorageTestHelper(storage) {
    @Test
    fun putInNested() = blocking {
        val child1 = storage.fork()
        val child2 = child1.fork()

        val value = DummyPerson.create()
        val query = where(Person::name).`is`(value.name)

        child2.put(SelectQuery(query), listOf(value))

        assertNull(storage.getIfPresent(SelectQuery(query)))
        assertNull(child1.getIfPresent(SelectQuery(query)))
        assertEquals(listOf(value), child2.getIfPresent(SelectQuery(query)))

        child1.put(SelectQuery(query), listOf(value))

        assertNull(storage.getIfPresent(SelectQuery(query)))
        assertEquals(listOf(value), child1.getIfPresent(SelectQuery(query)))
        assertEquals(listOf(value), child2.getIfPresent(SelectQuery(query)))

        storage.put(SelectQuery(query), listOf(value))

        assertEquals(listOf(value), storage.getIfPresent(SelectQuery(query)))
        assertEquals(listOf(value), child1.getIfPresent(SelectQuery(query)))
        assertEquals(listOf(value), child2.getIfPresent(SelectQuery(query)))
    }

    @Test
    fun clearInNested() = blocking {
        val child1 = storage.fork()
        val child2 = child1.fork()

        val value = DummyPerson.create()
        val query = where(Person::name).`is`(value.name)

        child2.put(SelectQuery(query), listOf(value))

        child1.clear()

        assertNull(storage.getIfPresent(SelectQuery(query)))
        assertNull(child1.getIfPresent(SelectQuery(query)))
        assertNull(child2.getIfPresent(SelectQuery(query)))

        child1.put(SelectQuery(query), listOf(value))

        storage.clear()

        assertNull(storage.getIfPresent(SelectQuery(query)))
        assertNull(child1.getIfPresent(SelectQuery(query)))
        assertNull(child2.getIfPresent(SelectQuery(query)))

        storage.put(SelectQuery(query), listOf(value))

        child2.clear()

        assertNull(storage.getIfPresent(SelectQuery(query)))
        assertNull(child1.getIfPresent(SelectQuery(query)))
        assertNull(child2.getIfPresent(SelectQuery(query)))
    }

    @Test
    fun merge() = blocking {
        val child1 = storage.fork()
        val child2 = child1.fork()

        val value = DummyPerson.create()
        val query = where(Person::name).`is`(value.name)

        child2.put(SelectQuery(query), listOf(value))

        assertNull(storage.getIfPresent(SelectQuery(query)))
        assertNull(child1.getIfPresent(SelectQuery(query)))
        assertEquals(listOf(value), child2.getIfPresent(SelectQuery(query)))

        child1.merge(child2)

        assertNull(storage.getIfPresent(SelectQuery(query)))
        assertEquals(listOf(value), child1.getIfPresent(SelectQuery(query)))
        assertEquals(listOf(value), child2.getIfPresent(SelectQuery(query)))

        storage.merge(child1)

        assertEquals(listOf(value), storage.getIfPresent(SelectQuery(query)))
        assertEquals(listOf(value), child1.getIfPresent(SelectQuery(query)))
        assertEquals(listOf(value), child2.getIfPresent(SelectQuery(query)))
    }

    @Test
    fun fork() = blocking {
        val child1 = storage.fork()
        val child2 = child1.fork()

        val value = DummyPerson.create()
        val query = where(Person::name).`is`(value.name)

        storage.put(SelectQuery(query), listOf(value))

        assertEquals(listOf(value), storage.getIfPresent(SelectQuery(query)))
        assertEquals(listOf(value), child1.getIfPresent(SelectQuery(query)))
        assertEquals(listOf(value), child2.getIfPresent(SelectQuery(query)))
    }
}
