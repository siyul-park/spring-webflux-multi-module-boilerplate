package io.github.siyual_park.data.repository.mongo

import io.github.siyual_park.data.dummy.DummyPerson
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.expansion.fieldName
import io.github.siyual_park.data.repository.RepositoryTestHelper
import io.github.siyual_park.data.repository.mongo.migration.CreatePerson
import io.github.siyual_park.data.test.MongoTestHelper
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.where

abstract class MongoRepositoryTestHelper(
    repositories: (RepositoryTestHelper<MongoRepository<Person, ULID>>) -> List<MongoRepository<Person, ULID>>,
) : RepositoryTestHelper<MongoRepository<Person, ULID>>(repositories) {

    init {
        migrationManager.register(CreatePerson(mongoTemplate))
    }

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

        assertEquals(person.name, foundPersons[0].name)
        assertEquals(person.age, foundPersons[0].age)
    }

    @Test
    fun findAllByNameIn() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPersons = personRepository.findAll(where(Person::name).`in`(person.name)).toList()

        assertEquals(foundPersons.size, 1)
        assertEquals(person.id, foundPersons[0].id)

        assertEquals(person.name, foundPersons[0].name)
        assertEquals(person.age, foundPersons[0].age)
    }

    @Test
    fun findOneByName() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPerson = personRepository.findOneOrFail(where(Person::name).`is`(person.name))

        assertEquals(person.id, foundPerson.id)

        assertEquals(person.name, foundPerson.name)
        assertEquals(person.age, foundPerson.age)
    }

    @Test
    fun updateByName() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val patch = DummyPerson.create()

        val updatedPerson = personRepository.updateOrFail(where(Person::name).`is`(person.name)) {
            it.name = patch.name
            it.age = patch.age
        }

        assertEquals(person.id, updatedPerson.id)
        assertEquals(patch.name, updatedPerson.name)
        assertEquals(patch.age, updatedPerson.age)
        assertNotNull(updatedPerson.updatedAt)
    }

    @Test
    fun updateByNameWithUpdate() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val patch = DummyPerson.create()

        val updatedPerson = personRepository.updateOrFail(
            where(Person::name).`is`(person.name),
            Update.update(fieldName(Person::name), patch.name)
        )

        assertEquals(person.id, updatedPerson.id)
        assertEquals(patch.name, updatedPerson.name)
        assertEquals(person.age, updatedPerson.age)
        assertNotNull(updatedPerson.updatedAt)
    }

    @Test
    fun updateWithUpdate() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val patch = DummyPerson.create()

        val updatedPerson = personRepository.updateOrFail(
            person,
            Update.update(fieldName(Person::name), patch.name)
        )

        assertEquals(person.id, updatedPerson.id)
        assertEquals(patch.name, updatedPerson.name)
        assertEquals(person.age, updatedPerson.age)
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
        Assertions.assertFalse(personRepository.existsById(person.id))
    }

    companion object {
        private val helper = MongoTestHelper()

        val mongoTemplate: ReactiveMongoTemplate
            get() = helper.mongoTemplate

        @BeforeAll
        @JvmStatic
        fun setUpAll() = helper.setUp()

        @AfterAll
        @JvmStatic
        fun tearDownAll() = helper.tearDown()
    }
}
