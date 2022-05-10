package io.github.siyual_park.data.transaction

import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.GenericReactiveTransaction
import org.springframework.transaction.support.TransactionTemplate
import reactor.core.publisher.Mono

@Suppress("ReactiveStreamsUnusedPublisher")
class ReactiveChainedTransactionManagerTest : CoroutineTestHelper() {

    @Test
    fun getReactiveTransaction() = blocking {
        val reactiveChainedTransactionManager = ReactiveChainedTransactionManager()

        val reactiveTransactionManager = mockk<ReactiveTransactionManager>()
        val transactionTemplate = TransactionTemplate()
        val genericReactiveTransaction = GenericReactiveTransaction(
            null,
            false,
            false,
            false,
            true,
            null
        )

        every { reactiveTransactionManager.getReactiveTransaction(transactionTemplate) } returns Mono.just(genericReactiveTransaction)

        reactiveChainedTransactionManager.registerTransactionManager(reactiveTransactionManager)

        val reactiveTransaction = reactiveChainedTransactionManager.getReactiveTransaction(transactionTemplate).awaitSingle()

        assertEquals(false, reactiveTransaction.isNewTransaction)
        assertEquals(false, reactiveTransaction.isRollbackOnly)
        assertEquals(false, reactiveTransaction.isCompleted)
    }

    @Test
    fun commit() = blocking {
        val reactiveChainedTransactionManager = ReactiveChainedTransactionManager()

        val reactiveTransactionManager = mockk<ReactiveTransactionManager>()
        val transactionTemplate = TransactionTemplate()
        val genericReactiveTransaction = GenericReactiveTransaction(
            null,
            false,
            false,
            false,
            true,
            null
        )

        every { reactiveTransactionManager.getReactiveTransaction(transactionTemplate) } returns Mono.just(genericReactiveTransaction)
        every { reactiveTransactionManager.commit(genericReactiveTransaction) } returns Mono.empty()

        reactiveChainedTransactionManager.registerTransactionManager(reactiveTransactionManager)

        val reactiveTransaction = reactiveChainedTransactionManager.getReactiveTransaction(transactionTemplate).awaitSingle()
        reactiveChainedTransactionManager.commit(reactiveTransaction).awaitSingleOrNull()

        verify { reactiveTransactionManager.commit(genericReactiveTransaction) }
    }

    @Test
    fun rollback() = blocking {
        val reactiveChainedTransactionManager = ReactiveChainedTransactionManager()

        val reactiveTransactionManager = mockk<ReactiveTransactionManager>()
        val transactionTemplate = TransactionTemplate()
        val genericReactiveTransaction = GenericReactiveTransaction(
            null,
            false,
            false,
            false,
            true,
            null
        )

        every { reactiveTransactionManager.getReactiveTransaction(transactionTemplate) } returns Mono.just(genericReactiveTransaction)
        every { reactiveTransactionManager.rollback(genericReactiveTransaction) } returns Mono.empty()

        reactiveChainedTransactionManager.registerTransactionManager(reactiveTransactionManager)

        val reactiveTransaction = reactiveChainedTransactionManager.getReactiveTransaction(transactionTemplate).awaitSingle()
        reactiveChainedTransactionManager.rollback(reactiveTransaction).awaitSingleOrNull()

        verify { reactiveTransactionManager.rollback(genericReactiveTransaction) }
    }
}
