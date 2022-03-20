package io.github.siyual_park.persistence

import io.github.siyual_park.data.repository.findByIdOrFail
import io.github.siyual_park.data.repository.r2dbc.CachedR2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.SimpleR2DBCRepository
import io.github.siyual_park.data.test.R2DBCTest
import io.github.siyual_park.persistence.domain.PersonAggregate
import io.github.siyual_park.persistence.dummy.DummyPerson
import io.github.siyual_park.persistence.entity.Person
import io.github.siyual_park.persistence.migration.CreatePerson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.transaction.reactive.executeAndAwait

class AggregateTest : R2DBCTest() {
    init {
        migrationManager.register(CreatePerson())
    }

    @Test
    fun testSync() = parameterized { personRepository ->
        val originPerson = DummyPerson.create()
        val person = personRepository.create(originPerson)
            .let { PersonAggregate(it, personRepository) }

        val updatedPerson = DummyPerson.create()

        var oldPerson = personRepository.findByIdOrFail(person.id!!)

        assertEquals(originPerson.name, oldPerson.name)
        assertEquals(originPerson.age, oldPerson.age)

        person.name = updatedPerson.name
        person.age = updatedPerson.age

        assertEquals(updatedPerson.name, person.name)
        assertEquals(updatedPerson.age, person.age)

        oldPerson = personRepository.findByIdOrFail(person.id!!)

        assertEquals(originPerson.name, oldPerson.name)
        assertEquals(originPerson.age, oldPerson.age)

        assertTrue(person.sync())

        oldPerson = personRepository.findByIdOrFail(person.id!!)

        assertEquals(updatedPerson.name, oldPerson.name)
        assertEquals(updatedPerson.age, oldPerson.age)

        assertEquals(updatedPerson.name, person.name)
        assertEquals(updatedPerson.age, person.age)
    }

    @Test
    fun testLink() = blocking {
        repositories().forEach { personRepository ->
            val person = DummyPerson.create()
                .let { personRepository.create(it) }
                .let { PersonAggregate(it, personRepository) }
            val updatedPerson = DummyPerson.create()

            transactionalOperator.executeAndAwait {
                person.link()

                person.name = updatedPerson.name
                person.age = updatedPerson.age
            }

            val oldPerson = personRepository.findByIdOrFail(person.id!!)

            assertEquals(updatedPerson.name, oldPerson.name)
            assertEquals(updatedPerson.age, oldPerson.age)
        }
    }

    private fun parameterized(func: suspend (R2DBCRepository<Person, Long>) -> Unit) {
        transactional {
            repositories().forEach {
                func(it)
                migrationManager.revert()
                migrationManager.run()
            }
        }
        blocking {
            repositories().forEach {
                func(it)
                migrationManager.revert()
                migrationManager.run()
            }
        }
    }

    private fun repositories(): List<R2DBCRepository<Person, Long>> {
        return listOf(
            SimpleR2DBCRepository(entityOperations, Person::class),
            CachedR2DBCRepository.of(entityOperations, Person::class)
        )
    }
}
