package io.github.siyual_park.data.repository

import io.github.siyual_park.data.dummy.DummyPerson
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.patch.SuspendPatch
import io.github.siyual_park.data.test.DataTestHelper
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.core.convert.converter.Converter

abstract class RepositoryTestHelper<R : Repository<Person, ULID>>(
    protected val repositories: (RepositoryTestHelper<R>) -> List<R>,
    converters: Collection<Converter<*, *>> = emptyList()
) : DataTestHelper(converters) {

    @Test
    fun create() = parameterized { personRepository ->
        val person = DummyPerson.create()
        val savedPerson = personRepository.create(person)

        assertNotNull(savedPerson.id)
        assertNotNull(savedPerson.createdAt)

        assertEquals(person.name, savedPerson.name)
        assertEquals(person.age, savedPerson.age)
    }

    @Test
    fun createAllWithFlow() = parameterized { personRepository ->
        val numOfPerson = 10

        val persons = (0 until numOfPerson).map { DummyPerson.create() }
        val savedPersons = personRepository.createAll(persons.asFlow()).toList()

        assertEquals(persons.size, savedPersons.size)
        for (i in 0 until numOfPerson) {
            val person = persons[i]
            val savedPerson = savedPersons[i]

            assertNotNull(savedPerson.id)
            assertNotNull(savedPerson.createdAt)

            assertEquals(person.name, savedPerson.name)
            assertEquals(person.age, savedPerson.age)
        }
    }

    @Test
    fun createAllWithIterable() = parameterized { personRepository ->
        val numOfPerson = 10

        val persons = (0 until numOfPerson).map { DummyPerson.create() }
        val savedPersons = personRepository.createAll(persons).toList()

        assertEquals(persons.size, savedPersons.size)
        for (i in 0 until numOfPerson) {
            val person = persons[i]
            val savedPerson = savedPersons[i]

            assertNotNull(savedPerson.id)
            assertNotNull(savedPerson.createdAt)

            assertEquals(person.name, savedPerson.name)
            assertEquals(person.age, savedPerson.age)
        }
    }

    @Test
    fun existsById() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }

        assertTrue(personRepository.existsById(person.id))
    }

    @Test
    fun findById() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPerson = personRepository.findById(person.id)!!

        assertEquals(person.id, foundPerson.id)

        assertEquals(person.name, foundPerson.name)
        assertEquals(person.age, foundPerson.age)
    }

    @Test
    fun findAll() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPersons = personRepository.findAll().toList()

        assertEquals(foundPersons.size, 1)
        assertEquals(person.id, foundPersons[0].id)

        assertEquals(person.name, foundPersons[0].name)
        assertEquals(person.age, foundPersons[0].age)
    }

    @Test
    fun findAllById() = parameterized { personRepository ->
        val numOfPerson = 10

        val persons = (0 until numOfPerson).map { DummyPerson.create() }
            .let { personRepository.createAll(it) }
            .toList()
        val ids = persons.map { it.id }

        val foundPersons = personRepository.findAllById(ids).toList()

        assertEquals(persons.size, foundPersons.size)
        for (i in 0 until numOfPerson) {
            val person = persons[i]
            val foundPerson = foundPersons[i]

            assertNotNull(foundPerson.id)
            assertNotNull(foundPerson.createdAt)

            assertEquals(person.name, foundPerson.name)
            assertEquals(person.age, foundPerson.age)
        }
    }

    @Test
    fun updateByIdWithPatch() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val person2 = DummyPerson.create()

        val updatedPerson = personRepository.updateById(
            person.id,
            Patch.with {
                it.name = person2.name
                it.age = person2.age
            }
        )!!

        assertEquals(person.id, updatedPerson.id)
        assertNotNull(updatedPerson.updatedAt)

        assertEquals(person2.name, updatedPerson.name)
        assertEquals(person2.age, updatedPerson.age)
    }

    @Test
    fun updateByIdWithAsyncPatch() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val person2 = DummyPerson.create()

        val updatedPerson = personRepository.updateById(
            person.id,
            SuspendPatch.with {
                it.name = person2.name
                it.age = person2.age
            }
        )!!

        assertEquals(person.id, updatedPerson.id)
        assertNotNull(updatedPerson.updatedAt)

        assertEquals(person2.name, updatedPerson.name)
        assertEquals(person2.age, updatedPerson.age)
    }

    @Test
    fun update() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val person2 = DummyPerson.create()

        person.name = person2.name
        person.age = person2.age

        val updatedPerson = personRepository.update(person)!!

        assertEquals(person.id, updatedPerson.id)
        assertNotNull(updatedPerson.updatedAt)

        assertEquals(person.name, updatedPerson.name)
        assertEquals(person.age, updatedPerson.age)
    }

    @Test
    fun updateWithPatch() = parameterized { personRepository ->
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
        assertNotNull(updatedPerson.updatedAt)

        assertEquals(person2.name, updatedPerson.name)
        assertEquals(person2.age, updatedPerson.age)
    }

    @Test
    fun updateWithAsyncPatch() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val person2 = DummyPerson.create()

        val updatedPerson = personRepository.update(
            person,
            SuspendPatch.with {
                it.name = person2.name
                it.age = person2.age
            }
        )!!

        assertEquals(person.id, updatedPerson.id)
        assertNotNull(updatedPerson.updatedAt)

        assertEquals(person2.name, updatedPerson.name)
        assertEquals(person2.age, updatedPerson.age)
    }

    @Test
    fun updateAllByIdWithPatch() = parameterized { personRepository ->
        val numOfPerson = 10

        val persons = (0 until numOfPerson)
            .map { DummyPerson.create() }
            .let { personRepository.createAll(it) }
            .toList()

        val personPatches = (0 until numOfPerson)
            .map { DummyPerson.create() }
            .toList()

        var current = 0
        val updatedPersons = personRepository.updateAllById(
            persons.map { it.id },
            Patch.with {
                val patch = personPatches[current++]
                it.name = patch.name
                it.age = patch.age
            }
        ).toList()

        assertEquals(persons.size, updatedPersons.size)
        for (i in 0 until numOfPerson) {
            val updatedPerson = updatedPersons[i]!!
            val patch = personPatches[i]

            assertNotNull(updatedPerson.id)
            assertNotNull(updatedPerson.createdAt)
            assertNotNull(updatedPerson.updatedAt)

            assertEquals(patch.name, updatedPerson.name)
            assertEquals(patch.age, updatedPerson.age)
        }
    }

    @Test
    fun updateAllByIdWithAsyncPatch() = parameterized { personRepository ->
        val numOfPerson = 10

        val persons = (0 until numOfPerson)
            .map { DummyPerson.create() }
            .let { personRepository.createAll(it) }
            .toList()

        val personPatches = (0 until numOfPerson)
            .map { DummyPerson.create() }
            .toList()

        var current = 0
        val updatedPersons = personRepository.updateAllById(
            persons.map { it.id },
            SuspendPatch.with {
                val patch = personPatches[current++]
                it.name = patch.name
                it.age = patch.age
            }
        ).toList()

        assertEquals(persons.size, updatedPersons.size)
        for (i in 0 until numOfPerson) {
            val updatedPerson = updatedPersons[i]!!
            val patch = personPatches[i]

            assertNotNull(updatedPerson.id)
            assertNotNull(updatedPerson.createdAt)
            assertNotNull(updatedPerson.updatedAt)

            assertEquals(patch.name, updatedPerson.name)
            assertEquals(patch.age, updatedPerson.age)
        }
    }

    @Test
    fun updateAll() = parameterized { personRepository ->
        val numOfPerson = 10

        var persons = (0 until numOfPerson)
            .map { DummyPerson.create() }
            .let { personRepository.createAll(it) }
            .toList()

        persons = persons.map {
            val person2 = DummyPerson.create()
            it.name = person2.name
            it.age = person2.age
            it
        }

        val updatedPersons = personRepository.updateAll(persons).toList()

        assertEquals(persons.size, updatedPersons.size)
        for (i in 0 until numOfPerson) {
            val person = persons[i]
            val updatedPerson = updatedPersons[i]!!

            assertNotNull(updatedPerson.id)

            assertEquals(person.name, updatedPerson.name)
            assertEquals(person.age, updatedPerson.age)
        }
    }

    @Test
    fun updateAllWithPatch() = parameterized { personRepository ->
        val numOfPerson = 10

        val persons = (0 until numOfPerson)
            .map { DummyPerson.create() }
            .let { personRepository.createAll(it) }
            .toList()

        val personPatches = (0 until numOfPerson)
            .map { DummyPerson.create() }
            .toList()

        var current = 0
        val updatedPersons = personRepository.updateAll(
            persons,
            Patch.with {
                val patch = personPatches[current++]
                it.name = patch.name
                it.age = patch.age
            }
        ).toList()

        assertEquals(persons.size, updatedPersons.size)
        for (i in 0 until numOfPerson) {
            val updatedPerson = updatedPersons[i]!!
            val patch = personPatches[i]

            assertNotNull(updatedPerson.id)
            assertNotNull(updatedPerson.createdAt)
            assertNotNull(updatedPerson.updatedAt)

            assertEquals(patch.name, updatedPerson.name)
            assertEquals(patch.age, updatedPerson.age)
        }
    }

    @Test
    fun updateAllWithAsyncPatch() = parameterized { personRepository ->
        val numOfPerson = 10

        val persons = (0 until numOfPerson)
            .map { DummyPerson.create() }
            .let { personRepository.createAll(it) }
            .toList()

        val personPatches = (0 until numOfPerson)
            .map { DummyPerson.create() }
            .toList()

        var current = 0
        val updatedPersons = personRepository.updateAll(
            persons,
            SuspendPatch.with {
                val patch = personPatches[current++]
                it.name = patch.name
                it.age = patch.age
            }
        ).toList()

        assertEquals(persons.size, updatedPersons.size)
        for (i in 0 until numOfPerson) {
            val updatedPerson = updatedPersons[i]!!
            val patch = personPatches[i]

            assertNotNull(updatedPerson.id)
            assertNotNull(updatedPerson.createdAt)
            assertNotNull(updatedPerson.updatedAt)

            assertEquals(patch.name, updatedPerson.name)
            assertEquals(patch.age, updatedPerson.age)
        }
    }

    @Test
    fun count() = parameterized { personRepository ->
        assertEquals(personRepository.count(), 0L)

        val numOfPerson = 10
        val persons = (0 until numOfPerson)
            .map { DummyPerson.create() }
            .let { personRepository.createAll(it) }
            .toList()

        assertEquals(personRepository.count(), persons.size.toLong())
    }

    @Test
    fun delete() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }

        personRepository.delete(person)

        assertFalse(personRepository.existsById(person.id))
    }

    @Test
    fun deleteById() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }

        personRepository.deleteById(person.id)

        assertFalse(personRepository.existsById(person.id))
    }

    @Test
    fun deleteAll() = parameterized { personRepository ->
        DummyPerson.create()
            .let { personRepository.create(it) }

        personRepository.deleteAll()

        assertEquals(0, personRepository.count())
    }

    @Test
    fun deleteAllById() = parameterized { personRepository ->
        val numOfPerson = 10

        val persons = (0 until numOfPerson)
            .map { DummyPerson.create() }
            .let { personRepository.createAll(it) }
            .toList()
        val ids = persons.map { it.id }

        personRepository.deleteAllById(ids)

        assertEquals(0, personRepository.count())
    }

    @Test
    fun deleteAllByEntity() = parameterized { personRepository ->
        val numOfPerson = 10

        val persons = (0 until numOfPerson)
            .map { DummyPerson.create() }
            .let { personRepository.createAll(it) }
            .toList()

        personRepository.deleteAll(persons)

        assertEquals(0, personRepository.count())
    }

    protected fun parameterized(func: suspend (R) -> Unit) {
        blocking {
            repositories(this@RepositoryTestHelper).forEach {
                func(it)
                migrationManager.revert()
                migrationManager.run()
            }
        }
    }
}
