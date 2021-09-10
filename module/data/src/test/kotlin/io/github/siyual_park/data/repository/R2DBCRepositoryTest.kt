package io.github.siyual_park.data.repository

import io.github.siyual_park.data.R2DBCTest
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.factory.PersonFactory
import io.github.siyual_park.data.migration.CreatePersonCheckpoint
import io.github.siyual_park.data.mock.Person
import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class R2DBCRepositoryTest : R2DBCTest() {
    private val personRepository = R2DBCRepository<Person, Long>(
        connectionFactory,
        Person::class
    )
    private val personFactory = PersonFactory()

    init {
        migrationManager.register(CreatePersonCheckpoint())
    }

    @Test
    fun create() = blocking {
        val person = personFactory.create()
        val savedPerson = personRepository.create(person)

        assertNotNull(savedPerson.id)
        assertNotNull(savedPerson.createdAt)
        assertNotNull(savedPerson.updatedAt)

        assertEquals(person.name, savedPerson.name)
        assertEquals(person.age, savedPerson.age)
    }

    @Test
    fun createAll() = blocking {
        val numOfPerson = 10

        val persons = (0 until numOfPerson).map { personFactory.create() }
        val savedPersons = personRepository.createAll(persons).toList()

        assertEquals(persons.size, savedPersons.size)
        for (i in 0 until numOfPerson) {
            val person = persons[i]
            val savedPerson = savedPersons[i]

            assertNotNull(savedPerson.id)
            assertNotNull(savedPerson.createdAt)
            assertNotNull(savedPerson.updatedAt)

            assertEquals(person.name, savedPerson.name)
            assertEquals(person.age, savedPerson.age)
        }
    }

    @Test
    fun existsById() = blocking {
        val person = personFactory.create()
            .let { personRepository.create(it) }

        assertTrue(personRepository.existsById(person.id!!))
    }

    @Test
    fun findById() = blocking {
        val person = personFactory.create()
            .let { personRepository.create(it) }
        val foundPerson = personRepository.findById(person.id!!)!!

        assertEquals(person.id, foundPerson.id)
        assertEquals(person.createdAt, foundPerson.createdAt)
        assertEquals(person.updatedAt, foundPerson.updatedAt)

        assertEquals(person.name, foundPerson.name)
        assertEquals(person.age, foundPerson.age)
    }

    @Test
    fun findAll() = blocking {
        val person = personFactory.create()
            .let { personRepository.create(it) }
        val foundPersons = personRepository.findAll().toList()

        assertEquals(foundPersons.size, 1)
        assertEquals(person.id, foundPersons[0].id)
        assertEquals(person.createdAt, foundPersons[0].createdAt)
        assertEquals(person.updatedAt, foundPersons[0].updatedAt)

        assertEquals(person.name, foundPersons[0].name)
        assertEquals(person.age, foundPersons[0].age)
    }

    @Test
    fun findAllCustomQuery() = blocking {
        val person = personFactory.create()
            .let { personRepository.create(it) }
        val foundPersons = personRepository.findAll(where(Person::id).`is`(person.id!!)).toList()

        assertEquals(foundPersons.size, 1)
        assertEquals(person.id, foundPersons[0].id)
        assertEquals(person.createdAt, foundPersons[0].createdAt)
        assertEquals(person.updatedAt, foundPersons[0].updatedAt)

        assertEquals(person.name, foundPersons[0].name)
        assertEquals(person.age, foundPersons[0].age)
    }

    @Test
    fun findAllById() = blocking {
        val numOfPerson = 10

        val persons = (0 until numOfPerson).map { personFactory.create() }
            .let { personRepository.createAll(it) }
            .toList()
        val ids = persons.map { it.id!! }

        val foundPersons = personRepository.findAllById(ids).toList()

        assertEquals(persons.size, foundPersons.size)
        for (i in 0 until numOfPerson) {
            val person = persons[i]
            val foundPerson = foundPersons[i]

            assertNotNull(foundPerson.id)
            assertNotNull(foundPerson.createdAt)
            assertNotNull(foundPerson.updatedAt)

            assertEquals(person.name, foundPerson.name)
            assertEquals(person.age, foundPerson.age)
        }
    }

    @Test
    fun update() = blocking {
        val person = personFactory.create()
            .let { personRepository.create(it) }
        val person2 = personFactory.create()

        val originPerson = person.clone()

        person.name = person2.name
        person.age = person2.age

        val updatedPerson = personRepository.update(person)!!

        assertEquals(person.id, updatedPerson.id)
        assertEquals(person.createdAt, updatedPerson.createdAt)
        assertTrue(originPerson.updatedAt!! < updatedPerson.updatedAt!!)

        assertEquals(person.name, updatedPerson.name)
        assertEquals(person.age, updatedPerson.age)
    }

    @Test
    fun updateByPatch() = blocking {
        val person = personFactory.create()
            .let { personRepository.create(it) }
        val person2 = personFactory.create()

        val updatedPerson = personRepository.update(
            person,
            Patch.with {
                it.name = person2.name
                it.age = person2.age
            }
        )!!

        assertEquals(person.id, updatedPerson.id)
        assertEquals(person.createdAt, updatedPerson.createdAt)
        assertTrue(person.updatedAt!! < updatedPerson.updatedAt!!)

        assertEquals(person.name, updatedPerson.name)
        assertEquals(person.age, updatedPerson.age)
    }

    @Test
    fun updateByAsyncPatch() = blocking {
        val person = personFactory.create()
            .let { personRepository.create(it) }
        val person2 = personFactory.create()

        val updatedPerson = personRepository.update(
            person,
            AsyncPatch.with {
                it.name = person2.name
                it.age = person2.age
            }
        )!!

        assertEquals(person.id, updatedPerson.id)
        assertEquals(person.createdAt, updatedPerson.createdAt)
        assertTrue(person.updatedAt!! < updatedPerson.updatedAt!!)

        assertEquals(person.name, updatedPerson.name)
        assertEquals(person.age, updatedPerson.age)
    }

    @Test
    fun updateAll() = blocking {
        val numOfPerson = 10

        var persons = (0 until numOfPerson)
            .map { personFactory.create() }
            .let { personRepository.createAll(it) }
            .toList()

        val originPersons = persons.map { it.clone() }

        val person2 = personFactory.create()
        persons = persons.map {
            it.name = person2.name
            it.age = person2.age
            it
        }

        val updatedPersons = personRepository.updateAll(persons).toList()

        assertEquals(persons.size, updatedPersons.size)
        for (i in 0 until numOfPerson) {
            val person = persons[i]
            val originPerson = originPersons[i]
            val updatedPerson = updatedPersons[i]!!

            assertNotNull(updatedPerson.id)
            assertNotNull(updatedPerson.createdAt)
            assertTrue(originPerson.updatedAt!! < updatedPerson.updatedAt!!)

            assertEquals(person.name, updatedPerson.name)
            assertEquals(person.age, updatedPerson.age)
        }
    }

    @Test
    fun updateAllByPatch() = blocking {
        val numOfPerson = 10

        val person2 = personFactory.create()
        val persons = (0 until numOfPerson)
            .map { personFactory.create() }
            .let { personRepository.createAll(it) }
            .toList()

        val updatedPersons = personRepository.updateAll(
            persons,
            Patch.with {
                it.name = person2.name
                it.age = person2.age
            }
        ).toList()

        assertEquals(persons.size, updatedPersons.size)
        for (i in 0 until numOfPerson) {
            val person = persons[i]
            val updatedPerson = updatedPersons[i]!!

            assertNotNull(updatedPerson.id)
            assertNotNull(updatedPerson.createdAt)
            assertTrue(person.updatedAt!! < updatedPerson.updatedAt!!)

            assertEquals(person.name, updatedPerson.name)
            assertEquals(person.age, updatedPerson.age)
        }
    }

    @Test
    fun updateAllByAsyncPatch() = blocking {
        val numOfPerson = 10

        val person2 = personFactory.create()
        val persons = (0 until numOfPerson)
            .map { personFactory.create() }
            .let { personRepository.createAll(it) }
            .toList()

        val updatedPersons = personRepository.updateAll(
            persons,
            AsyncPatch.with {
                it.name = person2.name
                it.age = person2.age
            }
        ).toList()

        assertEquals(persons.size, updatedPersons.size)
        for (i in 0 until numOfPerson) {
            val person = persons[i]
            val updatedPerson = updatedPersons[i]!!

            assertNotNull(updatedPerson.id)
            assertNotNull(updatedPerson.createdAt)
            assertTrue(person.updatedAt!! < updatedPerson.updatedAt!!)

            assertEquals(person.name, updatedPerson.name)
            assertEquals(person.age, updatedPerson.age)
        }
    }

    @Test
    fun count() = blocking {
        assertEquals(personRepository.count(), 0L)

        val numOfPerson = 10
        val persons = (0 until numOfPerson)
            .map { personFactory.create() }
            .let { personRepository.createAll(it) }
            .toList()

        assertEquals(personRepository.count(), persons.size.toLong())
    }

    @Test
    fun delete() = blocking {
        val person = personFactory.create()
            .let { personRepository.create(it) }

        personRepository.delete(person)

        assertFalse(personRepository.existsById(person.id!!))
    }

    @Test
    fun deleteById() = blocking {
        val person = personFactory.create()
            .let { personRepository.create(it) }

        personRepository.deleteById(person.id!!)

        assertFalse(personRepository.existsById(person.id!!))
    }

    @Test
    fun deleteAll() = blocking {
        personFactory.create()
            .let { personRepository.create(it) }

        personRepository.deleteAll()

        assertEquals(0, personRepository.count())
    }

    @Test
    fun deleteAllById() = blocking {
        val numOfPerson = 10

        val persons = (0 until numOfPerson)
            .map { personFactory.create() }
            .let { personRepository.createAll(it) }
            .toList()
        val ids = persons.map { it.id!! }

        personRepository.deleteAllById(ids)

        assertEquals(0, personRepository.count())
    }

    @Test
    fun deleteAllByEntity() = blocking {
        val numOfPerson = 10

        val persons = (0 until numOfPerson)
            .map { personFactory.create() }
            .let { personRepository.createAll(it) }
            .toList()

        personRepository.deleteAll(persons)

        assertEquals(0, personRepository.count())
    }
}
