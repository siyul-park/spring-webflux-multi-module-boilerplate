package io.github.siyual_park.data.repository.cache

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.transaction.NoTransactionException
import org.springframework.transaction.reactive.TransactionContext
import org.springframework.transaction.reactive.TransactionContextManager
import java.util.Stack

class TransactionalStorageManager<T : Any, ID : Any>(
    override val root: NestedStorage<T, ID>,
    private val cacheTransactionSynchronization: CacheTransactionSynchronization<T, ID> = CacheTransactionSynchronization()
) : StorageManager<T, ID> {
    private val logger = LoggerFactory.getLogger(TransactionalStorageManager::class.java)

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
                storage = cacheTransactionSynchronization.getOrPut(current) {
                    val child = storage.fork()
                    logger.debug("Forked Cache Storage [parent: $storage, child: $child]")
                    child
                }
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
