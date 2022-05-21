package io.github.siyual_park.data.cache

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.data.dummy.DummyPerson
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.repository.Extractor
import io.github.siyual_park.ulid.ULID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NestedStorageTest : CoroutineTestHelper() {
    private val idExtractor = object : Extractor<Person, ULID> {
        override fun getKey(entity: Person): ULID {
            return entity.id
        }
    }
    private val storage = NestedStorage(
        LoadingPool {
            InMemoryStorage(
                { CacheBuilder.newBuilder() },
                idExtractor
            )
        },
        idExtractor
    )

    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            storage.clear()
            storage.createIndex(
                "name",
                object : Extractor<Person, String> {
                    override fun getKey(entity: Person): String {
                        return entity.name
                    }
                }
            )
        }
    }

    @Test
    fun put() = blocking {
        val child1 = storage.fork()
        val child2 = child1.fork()

        val person = DummyPerson.create()

        child2.put(person)

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
    fun remove() = blocking {
        val child1 = storage.fork()
        val child2 = child1.fork()

        val person = DummyPerson.create()

        storage.put(person)

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
    fun get() = blocking {
        val child1 = storage.fork()
        val child2 = child1.fork()

        val person1 = DummyPerson.create()
        val person2 = DummyPerson.create().also { it.id = person1.id }
        val person3 = DummyPerson.create().also { it.id = person1.id }

        storage.put(person1)

        assertEquals(person1, child2.getIfPresent(person1.id))
        assertEquals(person1, child2.getIfPresent("name", person1.name))

        assertEquals(person1, child1.getIfPresent(person1.id))
        assertEquals(person1, child1.getIfPresent("name", person1.name))

        assertEquals(person1, storage.getIfPresent(person1.id))
        assertEquals(person1, storage.getIfPresent("name", person1.name))

        child1.put(person2)

        assertEquals(person2, child2.getIfPresent(person1.id))
        assertEquals(person2, child2.getIfPresent("name", person2.name))

        assertEquals(person2, child1.getIfPresent(person1.id))
        assertEquals(person2, child1.getIfPresent("name", person2.name))

        assertEquals(person1, storage.getIfPresent(person1.id))
        assertEquals(person1, storage.getIfPresent("name", person1.name))

        child2.put(person3)

        assertEquals(person3, child2.getIfPresent(person1.id))
        assertEquals(person3, child2.getIfPresent("name", person3.name))

        assertEquals(person2, child1.getIfPresent(person1.id))
        assertEquals(person2, child1.getIfPresent("name", person2.name))

        assertEquals(person1, storage.getIfPresent(person1.id))
        assertEquals(person1, storage.getIfPresent("name", person1.name))
    }
}
