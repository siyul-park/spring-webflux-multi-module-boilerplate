package io.github.siyual_park.data.aggregation

import io.github.siyual_park.data.cache.SelectQuery
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.data.dummy.DummyPerson
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepositoryBuilder
import io.github.siyual_park.data.repository.r2dbc.migration.CreatePerson
import io.github.siyual_park.data.test.DataTestHelper
import io.github.siyual_park.ulid.ULID
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.flow.toSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class QueryFetcherTest : DataTestHelper() {
    data class TestCase(
        val queries: List<SelectQuery>,
        val results: List<Set<Person>>
    )

    private val repository = spyk(R2DBCRepositoryBuilder<Person, ULID>(entityOperations, Person::class).build())
    private val queryAggregator = spyk(QueryAggregator(repository, Person::class))

    init {
        migrationManager.register(CreatePerson(entityOperations))
    }

    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            queryAggregator.clear()
        }
    }

    @Test
    fun fetch() = blocking {

        val person1 = DummyPerson.create()
            .let { repository.create(it) }
        val person2 = DummyPerson.create()
            .let { repository.create(it) }

        val testCase = listOf(
            TestCase(
                queries = listOf(
                    SelectQuery(where(Person::name).`is`(person1.name)),
                    SelectQuery(where(Person::name).`is`(person2.name))
                ),
                results = listOf(setOf(person1), setOf(person2))
            ),
            TestCase(
                queries = listOf(
                    SelectQuery(where(Person::name).`is`(person1.name)),
                    SelectQuery(where(Person::name).`in`(person2.name))
                ),
                results = listOf(setOf(person1), setOf(person2))
            ),
            TestCase(
                queries = listOf(
                    SelectQuery(where(Person::name).`in`(person1.name, person2.name)),
                    SelectQuery(where(Person::name).`in`(person2.name))
                ),
                results = listOf(setOf(person1, person2), setOf(person2))
            ),
            TestCase(
                queries = listOf(
                    SelectQuery(where(Person::name).`in`(person1.name, person2.name)),
                    SelectQuery(where(Person::name).like(person2.name))
                ),
                results = listOf(setOf(person1, person2), setOf(person2))
            )
        )

        testCase.forEachIndexed { i, case ->
            queryAggregator.clear()

            case.queries.forEach { queryAggregator.link(it) }
            val fetchers = case.queries.map { QueryFetcher(it, queryAggregator) }

            fetchers.forEachIndexed { index, queryFetcher ->
                assertEquals(case.results[index], queryFetcher.fetch().toSet())
            }

            coVerify(exactly = i + 1) { repository.findAll(any()) }
        }
    }

    @Test
    fun clear() = blocking {
        val person = DummyPerson.create()
            .let { repository.create(it) }

        val query = SelectQuery(where(Person::name).`is`(person.name))

        queryAggregator.link(query)

        val fetcher = QueryFetcher(query, queryAggregator)

        fetcher.clear()

        coVerify(exactly = 1) { queryAggregator.clear(query) }
    }
}
