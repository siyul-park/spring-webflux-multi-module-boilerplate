package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.repository.cache.NestedStorage
import io.github.siyual_park.data.repository.cache.StorageManager
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.transaction.NoTransactionException
import org.springframework.transaction.reactive.TransactionContext
import org.springframework.transaction.reactive.TransactionContextManager
import java.util.Stack

class R2DBCStorageManager<T : Any, ID : Any>(
    override val root: NestedStorage<T, ID>,
    private val cacheTransactionSynchronization: CacheTransactionSynchronization<T, ID> = CacheTransactionSynchronization()
) : StorageManager<T, ID> {

    override suspend fun getCurrent(): NestedStorage<T, ID> {
        try {
            val context = TransactionContextManager.currentContext().awaitSingleOrNull() ?: return root
            val synchronizations = context.synchronizations ?: return root
            synchronizations.add(cacheTransactionSynchronization)

            val chains = chains(context)

            var current: TransactionContext?
            var storage = root
            while (chains.isNotEmpty()) {
                current = chains.pop()
                storage = cacheTransactionSynchronization.getOrPut(current) { storage.fork() }
            }

            return storage
        } catch (e: NoTransactionException) {
            return root
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
