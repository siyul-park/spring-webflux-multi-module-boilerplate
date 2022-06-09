package io.github.siyual_park.data.aggregation

import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.data.dummy.DummyPerson
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepositoryBuilder
import io.github.siyual_park.data.repository.r2dbc.migration.CreatePerson
import io.github.siyual_park.data.test.DataTestHelper
import io.github.siyual_park.ulid.ULID
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FetchContextProviderTest : DataTestHelper() {
    private val repository = spyk(R2DBCRepositoryBuilder<Person, ULID>(entityOperations, Person::class).build())
    private val contextProvider = FetchContextProvider()

    init {
        migrationManager.register(CreatePerson(entityOperations))
    }

    @Test
    fun get() = blocking {
        val person1 = DummyPerson.create()
            .let { repository.create(it) }
        val person2 = DummyPerson.create()
            .let { repository.create(it) }

        val query1 = where(Person::name).`is`(person1.name)
        val query2 = where(Person::name).`is`(person2.name)

        val context1 = contextProvider.get(repository, Person::class)
        val context2 = contextProvider.get(repository, Person::class)

        val fetcher1 = context1.join(query1)
        val fetcher2 = context2.join(query2)

        val result1 = fetcher1.fetch().toList()
        val result2 = fetcher2.fetch().toList()

        assertEquals(1, result1.size)
        assertEquals(person1, result1[0])
        assertEquals(1, result2.size)
        assertEquals(person2, result2[0])

        coVerify(exactly = 1) { repository.findAll(any()) }

        fetcher1.fetch().toList()
        coVerify(exactly = 2) { repository.findAll(any()) }
        fetcher2.fetch().toList()
        coVerify(exactly = 2) { repository.findAll(any()) }

        fetcher1.fetch().toList()
        fetcher1.fetch().toList()
        coVerify(exactly = 4) { repository.findAll(any()) }
    }
}
