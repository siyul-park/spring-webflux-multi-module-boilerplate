package io.github.siyual_park.data.repository

import io.github.siyual_park.data.dummy.DummyPerson
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.ulid.ULID
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.transaction.reactive.executeAndAwait

abstract class TransactionalQueryableRepositoryTestHelper(
    repositories: (RepositoryTestHelper<QueryableRepository<Person, ULID>>) -> List<QueryableRepository<Person, ULID>>,
) : QueryableRepositoryTestHelper(repositories) {

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
        repositories(this@TransactionalQueryableRepositoryTestHelper).forEach { personRepository ->
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
