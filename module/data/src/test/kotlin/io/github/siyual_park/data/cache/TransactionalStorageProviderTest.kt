package io.github.siyual_park.data.cache

import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.data.transaction.ReactiveChainedTransactionManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.GenericReactiveTransaction
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import reactor.core.publisher.Mono

@Suppress("ReactiveStreamsUnusedPublisher")
class TransactionalStorageProviderTest : CoroutineTestHelper() {
    class MockGeneralNestedStorage : GeneralNestedStorage<MockGeneralNestedStorage> {
        override val parent: MockGeneralNestedStorage
            get() = TODO("Not yet implemented")

        override suspend fun fork(): MockGeneralNestedStorage {
            TODO("Not yet implemented")
        }

        override suspend fun merge(storage: MockGeneralNestedStorage) {
            TODO("Not yet implemented")
        }

        override suspend fun clear() {
            TODO("Not yet implemented")
        }
    }

    @Test
    fun get() = blocking {
        val reactiveChainedTransactionManager = ReactiveChainedTransactionManager()
        val transactionalOperator = TransactionalOperator.create(reactiveChainedTransactionManager)

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

        val parentGeneralNestedStorage = mockk<MockGeneralNestedStorage>()
        val childGeneralNestedStorage = mockk<MockGeneralNestedStorage>()
        val transactionalStorageProvider = TransactionalStorageProvider(parentGeneralNestedStorage)

        coEvery { parentGeneralNestedStorage.fork() } returns childGeneralNestedStorage

        assertEquals(parentGeneralNestedStorage, transactionalStorageProvider.get())

        transactionalOperator.executeAndAwait {
            assertEquals(childGeneralNestedStorage, transactionalStorageProvider.get())
        }
    }
}
