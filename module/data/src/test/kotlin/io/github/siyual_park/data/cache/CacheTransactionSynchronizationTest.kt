package io.github.siyual_park.data.cache

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.data.WeekProperty
import io.github.siyual_park.data.dummy.DummyPerson
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.transaction.ReactiveChainedTransactionManager
import io.github.siyual_park.data.transaction.currentContextOrNull
import io.github.siyual_park.ulid.ULID
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.GenericReactiveTransaction
import org.springframework.transaction.reactive.TransactionContext
import org.springframework.transaction.reactive.TransactionSynchronization
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import reactor.core.publisher.Mono

@Suppress("ReactiveStreamsUnusedPublisher")
class CacheTransactionSynchronizationTest : CoroutineTestHelper() {
    @Test
    fun get() {
        val cacheTransactionSynchronization = CacheTransactionSynchronization<NestedStorage<Any, Any>>()
        val transactionContext = mockk<TransactionContext>()

        assertEquals(null, cacheTransactionSynchronization.get(transactionContext))
    }

    @Test
    fun put() {
        val cacheTransactionSynchronization = CacheTransactionSynchronization<NestedStorage<Any, Any>>()
        val transactionContext = mockk<TransactionContext>()
        val storage = mockk<NestedStorage<Any, Any>>()

        every { transactionContext.synchronizations } returns mutableSetOf()

        assertEquals(null, cacheTransactionSynchronization.get(transactionContext))
        cacheTransactionSynchronization.put(transactionContext, storage)
        assertEquals(storage, cacheTransactionSynchronization.get(transactionContext))
    }

    @Test
    fun afterCompletion() = blocking {
        val reactiveChainedTransactionManager = ReactiveChainedTransactionManager()
        val transactionalOperator = TransactionalOperator.create(reactiveChainedTransactionManager)
        val cacheTransactionSynchronization = CacheTransactionSynchronization<NestedStorage<ULID, Person>>()

        val reactiveTransactionManager = mockk<ReactiveTransactionManager>()
        val genericReactiveTransaction = GenericReactiveTransaction(
            null,
            false,
            false,
            false,
            true,
            null
        )

        every { reactiveTransactionManager.getReactiveTransaction(any()) } returns Mono.just(genericReactiveTransaction)
        every { reactiveTransactionManager.commit(any()) } returns Mono.empty()
        every { reactiveTransactionManager.rollback(any()) } returns Mono.empty()

        reactiveChainedTransactionManager.registerTransactionManager(reactiveTransactionManager)

        val idProperty = object : WeekProperty<Person, ULID?> {
            override fun get(entity: Person): ULID {
                return entity.id
            }
        }
        val storage = PoolingNestedStorage(
            Pool {
                InMemoryStorage(
                    { CacheBuilder.newBuilder() },
                    idProperty
                )
            },
            idProperty
        )

        transactionalOperator.executeAndAwait {
            val child = storage.fork().also { cacheTransactionSynchronization.put(currentContextOrNull()!!, it) }
            val person = DummyPerson.create()

            assertNull(child.getIfPresent(person.id))
            child.add(person)
            assertEquals(person, child.getIfPresent(person.id))
            assertNull(storage.getIfPresent(person.id))

            cacheTransactionSynchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED).awaitSingleOrNull()

            val diff = child.checkout()
            assertEquals(0, diff.size)
            assertEquals(person, child.getIfPresent(person.id))
            assertEquals(person, storage.getIfPresent(person.id))

            assertNull(cacheTransactionSynchronization.get(currentContextOrNull()!!))
        }

        transactionalOperator.executeAndAwait {
            val child = storage.fork().also { cacheTransactionSynchronization.put(currentContextOrNull()!!, it) }
            val person = DummyPerson.create()

            assertNull(child.getIfPresent(person.id))
            child.add(person)
            assertEquals(person, child.getIfPresent(person.id))
            assertNull(storage.getIfPresent(person.id))

            cacheTransactionSynchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK).awaitSingleOrNull()

            val diff = child.checkout()
            assertEquals(0, diff.size)
            assertNull(child.getIfPresent(person.id))
            assertNull(storage.getIfPresent(person.id))

            assertNull(cacheTransactionSynchronization.get(currentContextOrNull()!!))
        }
    }
}
