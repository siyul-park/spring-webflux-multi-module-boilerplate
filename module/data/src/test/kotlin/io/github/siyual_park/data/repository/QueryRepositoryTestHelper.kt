package io.github.siyual_park.data.repository

import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.data.dummy.DummyPerson
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

abstract class QueryRepositoryTestHelper(
    repositories: (RepositoryTestHelper<QueryRepository<Person, ULID>>) -> List<QueryRepository<Person, ULID>>,
) : RepositoryTestHelper<QueryRepository<Person, ULID>>(repositories) {

    @Test
    fun existsByName() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }

        assertTrue(personRepository.exists(where(Person::name).`is`(person.name)))
    }

    @Test
    fun findAllCustomQuery() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPersons = personRepository.findAll(where(Person::id).`is`(person.id)).toList()

        assertEquals(foundPersons.size, 1)
        assertEquals(person.id, foundPersons[0].id)
        assertNotNull(foundPersons[0].createdAt)
        assertNotNull(foundPersons[0].updatedAt)

        assertEquals(person.name, foundPersons[0].name)
        assertEquals(person.age, foundPersons[0].age)
    }

    @Test
    fun findAllByNameIs() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPersons = personRepository.findAll(where(Person::name).`is`(person.name)).toList()

        assertEquals(foundPersons.size, 1)
        assertEquals(person.id, foundPersons[0].id)
        assertNotNull(foundPersons[0].createdAt)
        assertNotNull(foundPersons[0].updatedAt)

        assertEquals(person.name, foundPersons[0].name)
        assertEquals(person.age, foundPersons[0].age)
    }

    @Test
    fun findAllByNameIn() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPersons = personRepository.findAll(where(Person::name).`in`(person.name)).toList()

        assertEquals(1, foundPersons.size)
        assertEquals(person.id, foundPersons[0].id)
        assertNotNull(foundPersons[0].createdAt)
        assertNotNull(foundPersons[0].updatedAt)

        assertEquals(person.name, foundPersons[0].name)
        assertEquals(person.age, foundPersons[0].age)
    }

    @Test
    fun findOneByName() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPerson = personRepository.findOneOrFail(where(Person::name).`is`(person.name))

        assertEquals(person.id, foundPerson.id)
        assertNotNull(foundPerson.createdAt)
        assertNotNull(foundPerson.updatedAt)

        assertEquals(person.name, foundPerson.name)
        assertEquals(person.age, foundPerson.age)
    }

    @Test
    fun updateByName() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val patch = DummyPerson.create()

        val updatedPerson = personRepository.updateOrFail(
            where(Person::name).`is`(person.name)
        ) {
            it.name = patch.name
            it.age = patch.age
        }

        assertEquals(person.id, updatedPerson.id)
        assertEquals(patch.name, updatedPerson.name)
        assertEquals(patch.age, updatedPerson.age)
        assertNotNull(updatedPerson.updatedAt)
    }

    @Test
    fun updateAllByName() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val patch = DummyPerson.create()

        val updatedPersons = personRepository.updateAll(
            where(Person::name).`is`(person.name),
            Patch.with {
                it.name = patch.name
                it.age = patch.age
            }
        ).toList()

        assertEquals(1, updatedPersons.size)
        val updatedPerson = updatedPersons[0]
        assertEquals(person.id, updatedPerson.id)
        assertEquals(patch.name, updatedPerson.name)
        assertEquals(patch.age, updatedPerson.age)
        assertNotNull(updatedPerson.updatedAt)
    }

    @Test
    fun countByName() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }

        assertEquals(1, personRepository.count(where(Person::name).`is`(person.name)))
    }

    @Test
    fun deleteAllByName() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }

        personRepository.deleteAll(where(Person::name).`is`(person.name))
        assertFalse(personRepository.existsById(person.id))
    }
}
