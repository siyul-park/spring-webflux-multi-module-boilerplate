package io.github.siyual_park.persistence

import io.github.siyual_park.persistence.dummy.DummyPerson
import io.github.siyual_park.persistence.entity.PersonData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class LazyMutableTest {
    @Test
    fun testGet() {
        val person = DummyPerson.create()
        val lazyMutable = LazyMutable.from(person)

        assertEquals(person.id, lazyMutable[PersonData::id])
        assertEquals(person.name, lazyMutable[PersonData::name])
        assertEquals(person.age, lazyMutable[PersonData::age])
        assertEquals(person.updatedAt, lazyMutable[PersonData::updatedAt])
        assertEquals(person.createdAt, lazyMutable[PersonData::createdAt])
    }

    @Test
    fun testSet() {
        val person = DummyPerson.create()
        val persistenceContainer = LazyMutable.from(person)

        val person2 = DummyPerson.create()

        persistenceContainer[PersonData::id] = person2.id
        persistenceContainer[PersonData::name] = person2.name
        persistenceContainer[PersonData::age] = person2.age
        persistenceContainer[PersonData::updatedAt] = person2.updatedAt
        persistenceContainer[PersonData::createdAt] = person2.createdAt

        assertNotEquals(person2.name, person.name)
        assertNotEquals(person2.age, person.age)

        assertEquals(person2.id, persistenceContainer[PersonData::id])
        assertEquals(person2.name, persistenceContainer[PersonData::name])
        assertEquals(person2.age, persistenceContainer[PersonData::age])
        assertEquals(person2.updatedAt, persistenceContainer[PersonData::updatedAt])
        assertEquals(person2.createdAt, persistenceContainer[PersonData::createdAt])
    }
}
