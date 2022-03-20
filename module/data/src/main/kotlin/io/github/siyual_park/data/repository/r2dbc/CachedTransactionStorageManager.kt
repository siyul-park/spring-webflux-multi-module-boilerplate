package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.repository.cache.NestedStorage
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.transaction.NoTransactionException
import org.springframework.transaction.reactive.TransactionContext
import org.springframework.transaction.reactive.TransactionContextManager
import java.util.Stack

class CachedTransactionStorageManager<T : Any, ID : Any>(
    private val storage: NestedStorage<T, ID>,
    private val cacheTransactionSynchronization: CacheTransactionSynchronization<T, ID> = CacheTransactionSynchronization()
) {

    suspend fun getCurrent(): NestedStorage<T, ID> {
        try {
            val context = TransactionContextManager.currentContext().awaitSingleOrNull() ?: return storage
            val synchronizations = context.synchronizations ?: return storage
            synchronizations.add(cacheTransactionSynchronization)

            val chains = chains(context)

            var current: TransactionContext?
            var storage = storage
            while (chains.isNotEmpty()) {
                current = chains.pop()
                storage = cacheTransactionSynchronization.getOrPut(current) { storage.fork() }
            }

            return storage
        } catch (e: NoTransactionException) {
            return storage
        }
    }

    private fun chains(context: TransactionContext): Stack<TransactionContext> {
        val stack = Stack<TransactionContext>()
        var current: TransactionContext? = context
        while (current != null) {
            stack.add(current)
            current = current.parent
        }

        return stack
    }
}
