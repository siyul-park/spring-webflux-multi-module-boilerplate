package io.github.siyual_park.data.cache

import io.github.siyual_park.data.dummy.DummyPerson
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.ulid.ULID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

abstract class NestedStorageTestHelper(
    private val storage: NestedStorage<ULID, Person>
) : StorageTestHelper(storage) {

    @Test
    fun putInNested() = blocking {
        storage.createIndex("name", nameIndex)

        val child1 = storage.fork()
        val child2 = child1.fork()

        val person = DummyPerson.create()

        child2.add(person)

        assertEquals(person, child2.getIfPresent(person.id))
        assertEquals(person, child2.getIfPresent("name", person.name))

        assertEquals(null, child1.getIfPresent(person.id))
        assertEquals(null, child1.getIfPresent("name", person.name))

        assertEquals(null, storage.getIfPresent(person.id))
        assertEquals(null, storage.getIfPresent("name", person.name))

        child1.merge(child2)

        assertEquals(person, child2.getIfPresent(person.id))
        assertEquals(person, child2.getIfPresent("name", person.name))

        assertEquals(person, child1.getIfPresent(person.id))
        assertEquals(person, child1.getIfPresent("name", person.name))

        assertEquals(null, storage.getIfPresent(person.id))
        assertEquals(null, storage.getIfPresent("name", person.name))

        storage.merge(child1)

        assertEquals(person, child2.getIfPresent(person.id))
        assertEquals(person, child2.getIfPresent("name", person.name))

        assertEquals(person, child1.getIfPresent(person.id))
        assertEquals(person, child1.getIfPresent("name", person.name))

        assertEquals(person, storage.getIfPresent(person.id))
        assertEquals(person, storage.getIfPresent("name", person.name))
    }

    @Test
    fun removeInNested() = blocking {
        storage.createIndex("name", nameIndex)

        val child1 = storage.fork()
        val child2 = child1.fork()

        val person = DummyPerson.create()

        storage.add(person)

        assertEquals(person, child2.getIfPresent(person.id))
        assertEquals(person, child2.getIfPresent("name", person.name))

        assertEquals(person, child1.getIfPresent(person.id))
        assertEquals(person, child1.getIfPresent("name", person.name))

        assertEquals(person, storage.getIfPresent(person.id))
        assertEquals(person, storage.getIfPresent("name", person.name))

        child2.remove(person.id)

        assertEquals(null, child2.getIfPresent(person.id))
        assertEquals(null, child2.getIfPresent("name", person.name))

        assertEquals(person, child1.getIfPresent(person.id))
        assertEquals(person, child1.getIfPresent("name", person.name))

        assertEquals(person, storage.getIfPresent(person.id))
        assertEquals(person, storage.getIfPresent("name", person.name))

        child1.merge(child2)

        assertEquals(null, child2.getIfPresent(person.id))
        assertEquals(null, child2.getIfPresent("name", person.name))

        assertEquals(null, child1.getIfPresent(person.id))
        assertEquals(null, child1.getIfPresent("name", person.name))

        assertEquals(person, storage.getIfPresent(person.id))
        assertEquals(person, storage.getIfPresent("name", person.name))

        storage.merge(child1)

        assertEquals(null, child2.getIfPresent(person.id))
        assertEquals(null, child2.getIfPresent("name", person.name))

        assertEquals(null, child1.getIfPresent(person.id))
        assertEquals(null, child1.getIfPresent("name", person.name))

        assertEquals(null, storage.getIfPresent(person.id))
        assertEquals(null, storage.getIfPresent("name", person.name))
    }

    @Test
    fun getInNested() = blocking {
        storage.createIndex("name", nameIndex)

        val child1 = storage.fork()
        val child2 = child1.fork()

        val person1 = DummyPerson.create()
        val person2 = DummyPerson.create().also { it.id = person1.id }
        val person3 = DummyPerson.create().also { it.id = person1.id }

        storage.add(person1)

        assertEquals(person1, child2.getIfPresent(person1.id))
        assertEquals(person1, child2.getIfPresent("name", person1.name))

        assertEquals(person1, child1.getIfPresent(person1.id))
        assertEquals(person1, child1.getIfPresent("name", person1.name))

        assertEquals(person1, storage.getIfPresent(person1.id))
        assertEquals(person1, storage.getIfPresent("name", person1.name))

        child1.add(person2)

        assertEquals(person2, child2.getIfPresent(person1.id))
        assertEquals(person2, child2.getIfPresent("name", person2.name))

        assertEquals(person2, child1.getIfPresent(person1.id))
        assertEquals(person2, child1.getIfPresent("name", person2.name))

        assertEquals(person1, storage.getIfPresent(person1.id))
        assertEquals(person1, storage.getIfPresent("name", person1.name))

        child2.add(person3)

        assertEquals(person3, child2.getIfPresent(person1.id))
        assertEquals(person3, child2.getIfPresent("name", person3.name))

        assertEquals(person2, child1.getIfPresent(person1.id))
        assertEquals(person2, child1.getIfPresent("name", person2.name))

        assertEquals(person1, storage.getIfPresent(person1.id))
        assertEquals(person1, storage.getIfPresent("name", person1.name))
    }
}
