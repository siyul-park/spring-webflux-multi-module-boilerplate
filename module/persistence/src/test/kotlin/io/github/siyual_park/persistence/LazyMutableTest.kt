package io.github.siyual_park.persistence

import io.github.siyual_park.persistence.dummy.DummyPerson
import io.github.siyual_park.persistence.entity.Person
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class LazyMutableTest {
    @Test
    fun testGet() {
        val person = DummyPerson.create()
        val lazyMutable = LazyMutable.from(person)

        assertEquals(person.id, lazyMutable[Person::id])
        assertEquals(person.name, lazyMutable[Person::name])
        assertEquals(person.age, lazyMutable[Person::age])
        assertEquals(person.updatedAt, lazyMutable[Person::updatedAt])
        assertEquals(person.createdAt, lazyMutable[Person::createdAt])
    }

    @Test
    fun testSet() {
        val person = DummyPerson.create()
        val persistenceContainer = LazyMutable.from(person)

        val person2 = DummyPerson.create()

        persistenceContainer[Person::id] = person2.id
        persistenceContainer[Person::name] = person2.name
        persistenceContainer[Person::age] = person2.age
        persistenceContainer[Person::updatedAt] = person2.updatedAt
        persistenceContainer[Person::createdAt] = person2.createdAt

        assertNotEquals(person2.name, person.name)
        assertNotEquals(person2.age, person.age)

        assertEquals(person2.id, persistenceContainer[Person::id])
        assertEquals(person2.name, persistenceContainer[Person::name])
        assertEquals(person2.age, persistenceContainer[Person::age])
        assertEquals(person2.updatedAt, persistenceContainer[Person::updatedAt])
        assertEquals(person2.createdAt, persistenceContainer[Person::createdAt])
    }
}
