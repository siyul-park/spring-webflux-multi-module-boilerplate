package io.github.siyual_park.data.cache

import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.transaction.NoTransactionException
import org.springframework.transaction.reactive.TransactionContext
import org.springframework.transaction.reactive.TransactionContextManager
import java.util.Stack

class TransactionalStorageProvider<S : GeneralNestedStorage<S>>(
    private val root: S,
) {
    private val cacheTransactionSynchronization = CacheTransactionSynchronization<S>()
    private val mutex = Mutex()

    private val logger = LoggerFactory.getLogger(TransactionalStorageProvider::class.java)

    suspend fun get(): S {
        try {
            val context = TransactionContextManager.currentContext().awaitSingleOrNull() ?: return root
            val currentStorage = cacheTransactionSynchronization.get(context)
            if (currentStorage != null) {
                return currentStorage
            }

            val chains = chains(context)

            var current: TransactionContext?
            var storage = root
            while (chains.isNotEmpty()) {
                current = chains.pop()
                storage = cacheTransactionSynchronization.get(current) ?: mutex.withLock {
                    cacheTransactionSynchronization.get(current) ?: run {
                        val child = storage.fork()
                        cacheTransactionSynchronization.put(current, child)
                        logger.debug("Forked Cache Storage [parent: $storage, child: $child]")
                        child
                    }
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
