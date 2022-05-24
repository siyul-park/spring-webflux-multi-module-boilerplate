package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.dummy.DummyPerson
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.repository.QueryRepositoryTestHelper
import io.github.siyual_park.data.repository.r2dbc.migration.CreatePerson
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.transaction.reactive.executeAndAwait

class R2DBCQueryRepositoryAdapterTest : QueryRepositoryTestHelper(
    repositories = { listOf(R2DBCQueryRepositoryAdapter(SimpleR2DBCRepository(EntityManager(it.entityOperations, Person::class)))) }
) {
    init {
        migrationManager.register(CreatePerson(entityOperations))
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
        repositories(this@R2DBCQueryRepositoryAdapterTest).forEach { personRepository ->
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
}
