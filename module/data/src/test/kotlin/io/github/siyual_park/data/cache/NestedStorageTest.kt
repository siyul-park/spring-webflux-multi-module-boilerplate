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
        Pool {
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
}
