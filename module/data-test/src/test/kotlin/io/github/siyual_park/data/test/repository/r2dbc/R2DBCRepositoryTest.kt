package io.github.siyual_park.data.test.repository.r2dbc

import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.repository.r2dbc.CachedR2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.SimpleR2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.findOneOrFail
import io.github.siyual_park.data.test.R2DBCTest
import io.github.siyual_park.data.test.dummy.DummyPerson
import io.github.siyual_park.data.test.entity.Person
import io.github.siyual_park.data.test.migration.CreatePerson
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(PER_CLASS)
class R2DBCRepositoryTest : R2DBCTest() {
    init {
        migrationManager.register(CreatePerson())
    }

    fun personRepositories(): List<R2DBCRepository<Person, Long>> = listOf(
        SimpleR2DBCRepository(entityOperations, Person::class),
        CachedR2DBCRepository.of(entityOperations, Person::class)
    )

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun create(personRepository: R2DBCRepository<Person, Long>) = transactional {
        val person = DummyPerson.create()
        val savedPerson = personRepository.create(person)

        assertNotNull(savedPerson.id)
        assertNotNull(savedPerson.createdAt)
        assertNotNull(savedPerson.updatedAt)

        assertEquals(person.name, savedPerson.name)
        assertEquals(person.age, savedPerson.age)
    }

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun createAll(personRepository: R2DBCRepository<Person, Long>) = transactional {
        val numOfPerson = 10

        val persons = (0 until numOfPerson).map { DummyPerson.create() }
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

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun existsById(personRepository: R2DBCRepository<Person, Long>) = transactional {
        val person = DummyPerson.create()
            .let { personRepository.create(it) }

        assertTrue(personRepository.existsById(person.id!!))
    }

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun findById(personRepository: R2DBCRepository<Person, Long>) = transactional {
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPerson = personRepository.findById(person.id!!)!!

        assertEquals(person.id, foundPerson.id)
        assertEquals(person.createdAt, foundPerson.createdAt)
        assertEquals(person.updatedAt, foundPerson.updatedAt)

        assertEquals(person.name, foundPerson.name)
        assertEquals(person.age, foundPerson.age)
    }

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun findAll(personRepository: R2DBCRepository<Person, Long>) = transactional {
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPersons = personRepository.findAll().toList()

        assertEquals(foundPersons.size, 1)
        assertEquals(person.id, foundPersons[0].id)
        assertEquals(person.createdAt, foundPersons[0].createdAt)
        assertEquals(person.updatedAt, foundPersons[0].updatedAt)

        assertEquals(person.name, foundPersons[0].name)
        assertEquals(person.age, foundPersons[0].age)
    }

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun findAllCustomQuery(personRepository: R2DBCRepository<Person, Long>) = transactional {
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPersons = personRepository.findAll(where(Person::id).`is`(person.id!!)).toList()

        assertEquals(foundPersons.size, 1)
        assertEquals(person.id, foundPersons[0].id)
        assertEquals(person.createdAt, foundPersons[0].createdAt)
        assertEquals(person.updatedAt, foundPersons[0].updatedAt)

        assertEquals(person.name, foundPersons[0].name)
        assertEquals(person.age, foundPersons[0].age)
    }

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun findAllByNameIs(personRepository: R2DBCRepository<Person, Long>) = transactional {
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPersons = personRepository.findAll(where(Person::name).`is`(person.name)).toList()

        assertEquals(foundPersons.size, 1)
        assertEquals(person.id, foundPersons[0].id)
        assertEquals(person.createdAt, foundPersons[0].createdAt)
        assertEquals(person.updatedAt, foundPersons[0].updatedAt)

        assertEquals(person.name, foundPersons[0].name)
        assertEquals(person.age, foundPersons[0].age)
    }

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun findAllByNameIn(personRepository: R2DBCRepository<Person, Long>) = transactional {
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPersons = personRepository.findAll(where(Person::name).`in`(person.name)).toList()

        assertEquals(foundPersons.size, 1)
        assertEquals(person.id, foundPersons[0].id)
        assertEquals(person.createdAt, foundPersons[0].createdAt)
        assertEquals(person.updatedAt, foundPersons[0].updatedAt)

        assertEquals(person.name, foundPersons[0].name)
        assertEquals(person.age, foundPersons[0].age)
    }

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun findOneByName(personRepository: R2DBCRepository<Person, Long>) = transactional {
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPerson = personRepository.findOneOrFail(where(Person::name).`is`(person.name))

        assertEquals(person.id, foundPerson.id)
        assertEquals(person.createdAt, foundPerson.createdAt)
        assertEquals(person.updatedAt, foundPerson.updatedAt)

        assertEquals(person.name, foundPerson.name)
        assertEquals(person.age, foundPerson.age)
    }

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun findAllById(personRepository: R2DBCRepository<Person, Long>) = transactional {
        val numOfPerson = 10

        val persons = (0 until numOfPerson).map { DummyPerson.create() }
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

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun update(personRepository: R2DBCRepository<Person, Long>) = transactional {
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val person2 = DummyPerson.create()

        val originPerson = person.copy()

        person.name = person2.name
        person.age = person2.age

        val updatedPerson = personRepository.update(person)!!

        assertEquals(person.id, updatedPerson.id)
        assertEquals(person.createdAt, updatedPerson.createdAt)
        assertTrue(originPerson.updatedAt!! < updatedPerson.updatedAt!!)

        assertEquals(person.name, updatedPerson.name)
        assertEquals(person.age, updatedPerson.age)
    }

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun updateByPatch(personRepository: R2DBCRepository<Person, Long>) = transactional {
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val person2 = DummyPerson.create()

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

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun updateByAsyncPatch(personRepository: R2DBCRepository<Person, Long>) = transactional {
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val person2 = DummyPerson.create()

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

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun updateAll(personRepository: R2DBCRepository<Person, Long>) = transactional {
        val numOfPerson = 10

        var persons = (0 until numOfPerson)
            .map { DummyPerson.create() }
            .let { personRepository.createAll(it) }
            .toList()

        val originPersons = persons.map { it.copy() }

        val person2 = DummyPerson.create()
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

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun updateAllByPatch(personRepository: R2DBCRepository<Person, Long>) = transactional {
        val numOfPerson = 10

        val person2 = DummyPerson.create()
        val persons = (0 until numOfPerson)
            .map { DummyPerson.create() }
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

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun updateAllByAsyncPatch(personRepository: R2DBCRepository<Person, Long>) = transactional {
        val numOfPerson = 10

        val person2 = DummyPerson.create()
        val persons = (0 until numOfPerson)
            .map { DummyPerson.create() }
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

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun count(personRepository: R2DBCRepository<Person, Long>) = transactional {
        assertEquals(personRepository.count(), 0L)

        val numOfPerson = 10
        val persons = (0 until numOfPerson)
            .map { DummyPerson.create() }
            .let { personRepository.createAll(it) }
            .toList()

        assertEquals(personRepository.count(), persons.size.toLong())
    }

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun delete(personRepository: R2DBCRepository<Person, Long>) = blocking {
        val person = DummyPerson.create()
            .let { personRepository.create(it) }

        personRepository.delete(person)

        assertFalse(personRepository.existsById(person.id!!))
    }

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun deleteById(personRepository: R2DBCRepository<Person, Long>) = transactional {
        val person = DummyPerson.create()
            .let { personRepository.create(it) }

        personRepository.deleteById(person.id!!)

        assertFalse(personRepository.existsById(person.id!!))
    }

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun deleteAll(personRepository: R2DBCRepository<Person, Long>) = transactional {
        DummyPerson.create()
            .let { personRepository.create(it) }

        personRepository.deleteAll()

        assertEquals(0, personRepository.count())
    }

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun deleteAllById(personRepository: R2DBCRepository<Person, Long>) = transactional {
        val numOfPerson = 10

        val persons = (0 until numOfPerson)
            .map { DummyPerson.create() }
            .let { personRepository.createAll(it) }
            .toList()
        val ids = persons.map { it.id!! }

        personRepository.deleteAllById(ids)

        assertEquals(0, personRepository.count())
    }

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun deleteAllByEntity(personRepository: R2DBCRepository<Person, Long>) = transactional {
        val numOfPerson = 10

        val persons = (0 until numOfPerson)
            .map { DummyPerson.create() }
            .let { personRepository.createAll(it) }
            .toList()

        personRepository.deleteAll(persons)

        assertEquals(0, personRepository.count())
    }

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun transactionCommit(personRepository: R2DBCRepository<Person, Long>) = blocking {
        var person: Person? = null

        transactional {
            person = DummyPerson.create()
                .let { personRepository.create(it) }
        }

        assertTrue(personRepository.existsById(person?.id!!))
        assertTrue(personRepository.findById(person?.id!!) != null)
    }

    @ParameterizedTest
    @MethodSource("personRepositories")
    fun transactionRollback(personRepository: R2DBCRepository<Person, Long>) = blocking {
        var person: Person? = null

        assertThrows<RuntimeException> {
            transactional {
                person = DummyPerson.create()
                    .let { personRepository.create(it) }
                throw RuntimeException()
            }
        }

        assertFalse(personRepository.existsById(person?.id!!))
        assertTrue(personRepository.findById(person?.id!!) == null)
    }
}
