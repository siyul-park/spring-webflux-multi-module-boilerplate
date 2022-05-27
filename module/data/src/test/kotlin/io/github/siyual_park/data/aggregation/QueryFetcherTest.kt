package io.github.siyual_park.data.aggregation

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.cache.InMemoryQueryStorage
import io.github.siyual_park.data.cache.Pool
import io.github.siyual_park.data.cache.PoolStore
import io.github.siyual_park.data.cache.PoolingNestedQueryStorage
import io.github.siyual_park.data.criteria.Criteria
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
import kotlinx.coroutines.sync.Mutex
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class QueryFetcherTest : DataTestHelper() {
    private val pool = PoolStore<Criteria>()
    private val store = PoolingNestedQueryStorage(Pool { InMemoryQueryStorage(Person::class) { CacheBuilder.newBuilder() } })
    private val repository = spyk(R2DBCRepositoryBuilder<Person, ULID>(entityOperations, Person::class).build())
    private val mutex = Mutex()

    init {
        migrationManager.register(CreatePerson(entityOperations))
    }

    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            store.clear()
            pool.clear()
        }
    }

    @Test
    fun fetch() = blocking {
        val person1 = DummyPerson.create()
            .let { repository.create(it) }
        val person2 = DummyPerson.create()
            .let { repository.create(it) }

        val criteria1 = where(Person::name).`is`(person1.name)
        val criteria2 = where(Person::name).`is`(person2.name)

        pool.push(criteria1)
        pool.push(criteria2)

        val fetcher1 = QueryFetcher(criteria1, pool, store, repository, Person::class, mutex)
        val fetcher2 = QueryFetcher(criteria2, pool, store, repository, Person::class, mutex)

        val result1 = fetcher1.fetch().toList()
        val result2 = fetcher2.fetch().toList()

        assertEquals(1, result1.size)
        assertEquals(person1, result1[0])
        assertEquals(1, result2.size)
        assertEquals(person2, result2[0])

        assertEquals(0, store.entries().size)

        coVerify(exactly = 1) { repository.findAll(any()) }

        fetcher1.fetch().toList()
        assertEquals(1, store.entries().size)
        coVerify(exactly = 2) { repository.findAll(any()) }

        fetcher2.fetch().toList()
        assertEquals(0, store.entries().size)
        coVerify(exactly = 2) { repository.findAll(any()) }

        fetcher1.fetch().toList()
        fetcher1.fetch().toList()
        assertEquals(1, store.entries().size)
        coVerify(exactly = 4) { repository.findAll(any()) }
    }
}
