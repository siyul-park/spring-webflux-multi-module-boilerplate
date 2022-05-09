package io.github.siyual_park.data.repository.r2dbc.repository.r2dbc

import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.dummy.DummyPerson
import io.github.siyual_park.data.repository.r2dbc.entity.Person
import io.github.siyual_park.data.repository.r2dbc.findOneOrFail
import io.github.siyual_park.data.repository.r2dbc.repository.RepositoryTestHelper
import io.github.siyual_park.data.repository.r2dbc.repository.r2dbc.migration.CreatePerson
import io.github.siyual_park.data.repository.r2dbc.where
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.transaction.reactive.executeAndAwait

abstract class R2DBCRepositoryTestHelper(
    repositories: (RepositoryTestHelper<R2DBCRepository<Person, ULID>>) -> List<R2DBCRepository<Person, ULID>>,
) : RepositoryTestHelper<R2DBCRepository<Person, ULID>>(repositories) {
    init {
        migrationManager.register(CreatePerson(entityOperations))
    }

    @Test
    fun findAllCustomQuery() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPersons = personRepository.findAll(where(Person::id).`is`(person.id)).toList()

        assertEquals(foundPersons.size, 1)
        assertEquals(person.id, foundPersons[0].id)
        assertEquals(person.createdAt, foundPersons[0].createdAt)
        assertEquals(person.updatedAt, foundPersons[0].updatedAt)

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
        assertEquals(person.createdAt, foundPersons[0].createdAt)
        assertEquals(person.updatedAt, foundPersons[0].updatedAt)

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
        assertEquals(person.createdAt, foundPersons[0].createdAt)
        assertEquals(person.updatedAt, foundPersons[0].updatedAt)

        assertEquals(person.name, foundPersons[0].name)
        assertEquals(person.age, foundPersons[0].age)
    }

    @Test
    fun findOneByName() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
        val foundPerson = personRepository.findOneOrFail(where(Person::name).`is`(person.name))

        assertEquals(person.id, foundPerson.id)
        assertEquals(person.createdAt, foundPerson.createdAt)
        assertEquals(person.updatedAt, foundPerson.updatedAt)

        assertEquals(person.name, foundPerson.name)
        assertEquals(person.age, foundPerson.age)
    }

    @Test
    fun transactionCommit() = parameterized { personRepository ->
        var person: Person? = null

        transactionalOperator.executeAndAwait {
            person = DummyPerson.create()
                .let { personRepository.create(it) }
        }

        assertTrue(personRepository.findById(person?.id!!) != null)
        assertTrue(personRepository.existsById(person?.id!!))
    }

    @Test
    fun transactionRollback() = blocking {
        repositories(this@R2DBCRepositoryTestHelper).forEach { personRepository ->
            var person: Person? = null

            assertThrows<RuntimeException> {
                transactionalOperator.executeAndAwait {
                    it.setRollbackOnly()
                    person = DummyPerson.create()
                        .let { personRepository.create(it) }
                    throw RuntimeException()
                }
            }

            assertTrue(personRepository.findById(person?.id!!) == null)
            assertFalse(personRepository.existsById(person?.id!!))
        }
    }

    // private fun repositories(): List<R2DBCRepository<Person, ULID>> {
    //     return listOf(
    //         SimpleR2DBCRepository(entityOperations, Person::class),
    //         R2DBCRepositoryBuilder<Person, ULID>(entityOperations, Person::class)
    //             .set(
    //                 CacheBuilder.newBuilder()
    //                     .softValues()
    //                     .expireAfterAccess(Duration.ofMinutes(2))
    //                     .expireAfterWrite(Duration.ofMinutes(5))
    //                     .maximumSize(1_000)
    //             )
    //             .build()
    //     )
    // }
}
