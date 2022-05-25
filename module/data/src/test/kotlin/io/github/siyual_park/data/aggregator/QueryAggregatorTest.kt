package io.github.siyual_park.data.aggregator

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.criteria.`is`
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.data.dummy.DummyPerson
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepositoryBuilder
import io.github.siyual_park.data.repository.r2dbc.migration.CreatePerson
import io.github.siyual_park.data.test.DataTestHelper
import io.github.siyual_park.test.DummyNameFactory
import io.github.siyual_park.ulid.ULID
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class QueryAggregatorTest : DataTestHelper() {
    init {
        migrationManager.register(CreatePerson(entityOperations))
    }

    private val repository = spyk(R2DBCRepositoryBuilder<Person, ULID>(entityOperations, Person::class).build())
    private val aggregator = QueryAggregator(repository, Person::class) { CacheBuilder.newBuilder() }

    @BeforeEach
    override fun setUp() {
        super.setUp()
        aggregator.clear()
    }

    @Test
    fun run() = blocking {
        val person1 = DummyPerson.create()
            .let { repository.create(it) }
        val person2 = DummyPerson.create()
            .let { repository.create(it) }

        val query1 = where(Person::name).`is`(person1.name)
        val query2 = where(Person::name).`is`(person2.name)

        val name = DummyNameFactory.create(10)

        val runner1 = aggregator.runner(name, query1)
        val runner2 = aggregator.runner(name, query2)

        assertEquals(person1, runner1.run().first())
        assertEquals(person2, runner2.run().first())

        coVerify(exactly = 1) { repository.findAll(any()) }
    }
}
