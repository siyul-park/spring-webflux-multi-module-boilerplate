package io.github.siyual_park.data.test.repository.cache

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.repository.cache.Extractor
import io.github.siyual_park.data.repository.cache.InMemoryNestedStorage
import io.github.siyual_park.data.repository.r2dbc.SimpleR2DBCRepository
import io.github.siyual_park.data.test.R2DBCTest
import io.github.siyual_park.data.test.entity.Person
import io.github.siyual_park.data.test.factory.PersonFactory
import io.github.siyual_park.data.test.migration.CreatePerson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("UNCHECKED_CAST")
class InMemoryNestedStorageTest : R2DBCTest() {
    private val personFactory = PersonFactory()
    private val personRepository = SimpleR2DBCRepository<Person, Long>(entityOperations, Person::class)

    private val storage = InMemoryNestedStorage(
        CacheBuilder.newBuilder() as CacheBuilder<Long, Person>,
        object : Extractor<Person, Long> {
            override fun getKey(entity: Person): Long? {
                return entity.id
            }
        }
    ).also {
        it.createIndex(
            "name",
            object : Extractor<Person, String> {
                override fun getKey(entity: Person): String {
                    return entity.name
                }
            }
        )
    }

    init {
        migrationManager.register(CreatePerson())
    }

    @BeforeEach
    override fun setUp() {
        super.setUp()

        storage.clear()
    }

    @Test
    fun put() = blocking {
        val child1 = storage.fork()
        val child2 = child1.fork()

        val person = personFactory.create()
            .let { personRepository.create(it) }

        child2.put(person)

        assertEquals(person, child2.getIfPresent(person.id!!))
        assertEquals(person, child2.getIfPresent(person.name, "name"))

        assertEquals(null, child1.getIfPresent(person.id!!))
        assertEquals(null, child1.getIfPresent(person.name, "name"))

        assertEquals(null, storage.getIfPresent(person.id!!))
        assertEquals(null, storage.getIfPresent(person.name, "name"))

        child1.join(child2)

        assertEquals(person, child2.getIfPresent(person.id!!))
        assertEquals(person, child2.getIfPresent(person.name, "name"))

        assertEquals(person, child1.getIfPresent(person.id!!))
        assertEquals(person, child1.getIfPresent(person.name, "name"))

        assertEquals(null, storage.getIfPresent(person.id!!))
        assertEquals(null, storage.getIfPresent(person.name, "name"))

        storage.join(child1)

        assertEquals(person, child2.getIfPresent(person.id!!))
        assertEquals(person, child2.getIfPresent(person.name, "name"))

        assertEquals(person, child1.getIfPresent(person.id!!))
        assertEquals(person, child1.getIfPresent(person.name, "name"))

        assertEquals(person, storage.getIfPresent(person.id!!))
        assertEquals(person, storage.getIfPresent(person.name, "name"))
    }

    @Test
    fun remove() = blocking {
        val child1 = storage.fork()
        val child2 = child1.fork()

        val person = personFactory.create()
            .let { personRepository.create(it) }

        storage.put(person)

        assertEquals(person, child2.getIfPresent(person.id!!))
        assertEquals(person, child2.getIfPresent(person.name, "name"))

        assertEquals(person, child1.getIfPresent(person.id!!))
        assertEquals(person, child1.getIfPresent(person.name, "name"))

        assertEquals(person, storage.getIfPresent(person.id!!))
        assertEquals(person, storage.getIfPresent(person.name, "name"))

        child2.remove(person.id!!)

        assertEquals(null, child2.getIfPresent(person.id!!))
        assertEquals(null, child2.getIfPresent(person.name, "name"))

        assertEquals(person, child1.getIfPresent(person.id!!))
        assertEquals(person, child1.getIfPresent(person.name, "name"))

        assertEquals(person, storage.getIfPresent(person.id!!))
        assertEquals(person, storage.getIfPresent(person.name, "name"))

        child1.join(child2)

        assertEquals(null, child2.getIfPresent(person.id!!))
        assertEquals(null, child2.getIfPresent(person.name, "name"))

        assertEquals(null, child1.getIfPresent(person.id!!))
        assertEquals(null, child1.getIfPresent(person.name, "name"))

        assertEquals(person, storage.getIfPresent(person.id!!))
        assertEquals(person, storage.getIfPresent(person.name, "name"))

        storage.join(child1)

        assertEquals(null, child2.getIfPresent(person.id!!))
        assertEquals(null, child2.getIfPresent(person.name, "name"))

        assertEquals(null, child1.getIfPresent(person.id!!))
        assertEquals(null, child1.getIfPresent(person.name, "name"))

        assertEquals(null, storage.getIfPresent(person.id!!))
        assertEquals(null, storage.getIfPresent(person.name, "name"))
    }
}
