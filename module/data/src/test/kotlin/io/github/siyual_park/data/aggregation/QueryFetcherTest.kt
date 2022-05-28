package io.github.siyual_park.data.aggregation

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.cache.InMemoryQueryStorage
import io.github.siyual_park.data.cache.ReferenceStore
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
import kotlinx.coroutines.sync.Mutex
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class QueryFetcherTest : DataTestHelper() {
    data class TestCase(
        val queries: List<SelectQuery>,
        val results: List<Set<Person>>
    )

    private val repository = spyk(R2DBCRepositoryBuilder<Person, ULID>(entityOperations, Person::class).build())
    private val mutex = Mutex()

    init {
        migrationManager.register(CreatePerson(entityOperations))
    }

    @BeforeEach
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun fetch() = blocking {
        val links = ReferenceStore<SelectQuery>()
        val store = spyk(InMemoryQueryStorage(Person::class) { CacheBuilder.newBuilder() })

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
            )
        )

        testCase.forEachIndexed { i, case ->
            links.clear()
            store.clear()

            case.queries.forEach { links.push(it) }
            val fetchers = case.queries.map { QueryFetcher(it, links, store, repository, Person::class, mutex) }

            fetchers.forEachIndexed { index, queryFetcher ->
                assertEquals(case.results[index], queryFetcher.fetch().toSet())
            }

            assertEquals(0, store.entries().size)
            coVerify(exactly = i + 1) { repository.findAll(any()) }
        }
    }

    @Test
    fun clear() = blocking {
        val links = ReferenceStore<SelectQuery>()
        val store = spyk(InMemoryQueryStorage(Person::class) { CacheBuilder.newBuilder() })

        val person1 = DummyPerson.create()
            .let { repository.create(it) }
        val person2 = DummyPerson.create()
            .let { repository.create(it) }

        val query1 = SelectQuery(where(Person::name).`is`(person1.name))
        val query2 = SelectQuery(where(Person::name).`is`(person2.name))
        val query3 = SelectQuery(where(Person::name).`in`(person1.name, person2.name))

        links.push(query1)
        links.push(query2)
        links.push(query3)

        val fetcher1 = QueryFetcher(query1, links, store, repository, Person::class, mutex)
        val fetcher2 = QueryFetcher(query2, links, store, repository, Person::class, mutex)
        val fetcher3 = QueryFetcher(query3, links, store, repository, Person::class, mutex)

        fetcher1.clear()

        assertEquals(0, store.entries().size)
        assertEquals(2, links.entries().size)

        store.clear()
        links.clear()

        links.push(query1)
        links.push(query2)
        links.push(query3)

        fetcher1.fetchOne()

        assertNotNull(store.getIfPresent(query2))
        assertNotNull(store.getIfPresent(query3))

        fetcher2.clear()

        assertNull(store.getIfPresent(query2))
        assertNull(store.getIfPresent(query3))

        store.clear()
        links.clear()

        links.push(query1)
        links.push(query2)
        links.push(query3)

        fetcher2.fetchOne()

        assertNotNull(store.getIfPresent(query1))
        assertNull(store.getIfPresent(query2))
        assertNotNull(store.getIfPresent(query3))

        fetcher3.clear()

        assertNull(store.getIfPresent(query1))
        assertNull(store.getIfPresent(query2))
        assertNull(store.getIfPresent(query3))

        store.clear()
        links.clear()

        links.push(query1)
        links.push(query2)
        links.push(query3)

        fetcher3.fetchOne()

        assertNotNull(store.getIfPresent(query1))
        assertNotNull(store.getIfPresent(query2))
        assertNull(store.getIfPresent(query3))

        fetcher1.clear()

        assertNotNull(store.getIfPresent(query2))
        assertNull(store.getIfPresent(query1))
        assertNull(store.getIfPresent(query3))
    }
}
