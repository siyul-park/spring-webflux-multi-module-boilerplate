package io.github.siyual_park.data.repository

import io.github.siyual_park.data.R2DBCTest
import io.github.siyual_park.data.migration.CreatePersonCheckpoint
import io.github.siyual_park.data.mock.Person
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class R2DBCRepositoryTest: R2DBCTest() {
    private val personRepository = R2DBCRepository<Person, Long>(
        connectionFactory,
        Person::class
    )

    init {
        migrationManager.register(CreatePersonCheckpoint())
    }

    @Test
    fun create() = async {
        val person = Person("Joe", 34)
        val savedPerson = personRepository.create(person)

        assertNotNull(savedPerson.id)
        assertNotNull(savedPerson.createdAt)
        assertEquals(person.name, savedPerson.name)
        assertEquals(person.age, savedPerson.age)
    }
}