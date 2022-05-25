package io.github.siyual_park.persistence

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.repository.QueryRepository
import io.github.siyual_park.data.repository.findByIdOrFail
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepositoryBuilder
import io.github.siyual_park.data.test.DataTestHelper
import io.github.siyual_park.persistence.domain.Person
import io.github.siyual_park.persistence.dummy.DummyPerson
import io.github.siyual_park.persistence.entity.PersonData
import io.github.siyual_park.persistence.migration.CreatePerson
import io.github.siyual_park.ulid.ULID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.transaction.reactive.executeAndAwait
import java.time.Duration

class PersistenceTest : DataTestHelper() {
    init {
        migrationManager.register(CreatePerson(entityOperations))
    }

    @Test
    fun sync() = parameterized { personRepository ->
        val originPerson = DummyPerson.create()
        val person = personRepository.create(originPerson)
            .let { Person(it, personRepository) }

        val updatedPerson = DummyPerson.create()

        var oldPerson = personRepository.findByIdOrFail(person.id)

        assertEquals(originPerson.name, oldPerson.name)
        assertEquals(originPerson.age, oldPerson.age)

        person.name = updatedPerson.name
        person.age = updatedPerson.age

        assertEquals(updatedPerson.name, person.name)
        assertEquals(updatedPerson.age, person.age)

        oldPerson = personRepository.findByIdOrFail(person.id)

        assertEquals(originPerson.name, oldPerson.name)
        assertEquals(originPerson.age, oldPerson.age)

        assertTrue(person.sync())

        oldPerson = personRepository.findByIdOrFail(person.id)

        assertEquals(updatedPerson.name, oldPerson.name)
        assertEquals(updatedPerson.age, oldPerson.age)

        assertEquals(updatedPerson.name, person.name)
        assertEquals(updatedPerson.age, person.age)
    }

    @Test
    fun link() = blocking {
        repositories().forEach { personRepository ->
            val person = DummyPerson.create()
                .let { personRepository.create(it) }
                .let { Person(it, personRepository) }
            val updatedPerson = DummyPerson.create()

            transactionalOperator.executeAndAwait {
                person.link()

                person.name = updatedPerson.name
                person.age = updatedPerson.age
            }

            val oldPerson = personRepository.findByIdOrFail(person.id)

            assertEquals(updatedPerson.name, oldPerson.name)
            assertEquals(updatedPerson.age, oldPerson.age)
        }
    }

    @Test
    fun clear() = parameterized { personRepository ->
        val person = DummyPerson.create()
            .let { personRepository.create(it) }
            .let { Person(it, personRepository) }

        assertTrue(personRepository.existsById(person.id))
        person.clear()
        assertFalse(personRepository.existsById(person.id))
    }

    private fun parameterized(func: suspend (QueryRepository<PersonData, ULID>) -> Unit) {
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

    private fun repositories(): List<QueryRepository<PersonData, ULID>> {
        return listOf(
            R2DBCRepositoryBuilder<PersonData, ULID>(entityOperations, PersonData::class).build(),
            R2DBCRepositoryBuilder<PersonData, ULID>(entityOperations, PersonData::class)
                .enableCache {
                    CacheBuilder.newBuilder()
                        .softValues()
                        .expireAfterAccess(Duration.ofMinutes(2))
                        .expireAfterWrite(Duration.ofMinutes(5))
                        .maximumSize(1_000)
                }
                .build(),
            R2DBCRepositoryBuilder<PersonData, ULID>(entityOperations, PersonData::class)
                .enableCache {
                    CacheBuilder.newBuilder()
                        .softValues()
                        .expireAfterAccess(Duration.ofMinutes(2))
                        .expireAfterWrite(Duration.ofMinutes(5))
                        .maximumSize(1_000)
                }
                .enableQueryCache {
                    CacheBuilder.newBuilder()
                        .softValues()
                        .expireAfterWrite(Duration.ofSeconds(1))
                        .maximumSize(1_000)
                }
                .build()
        )
    }
}
