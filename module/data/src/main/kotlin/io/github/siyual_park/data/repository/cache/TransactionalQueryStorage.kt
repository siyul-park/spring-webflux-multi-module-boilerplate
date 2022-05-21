package io.github.siyual_park.data.repository.cache

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.transaction.NoTransactionException
import org.springframework.transaction.reactive.TransactionContext
import org.springframework.transaction.reactive.TransactionContextManager
import java.util.Stack

class TransactionalQueryStorage<T : Any>(
    private val root: NestedQueryStorage<T>,
) : QueryStorage<T> {
    private val cacheTransactionSynchronization = CacheTransactionSynchronization<NestedQueryStorage<T>>()
    private val mutex = Mutex()

    private val logger = LoggerFactory.getLogger(TransactionalQueryStorage::class.java)

    override suspend fun getIfPresent(where: String): T? {
        return getCurrent().getIfPresent(where)
    }

    override suspend fun getIfPresent(where: String, loader: suspend () -> T?): T? {
        return getCurrent().getIfPresent(where, loader)
    }

    override fun getIfPresent(select: SelectQuery, loader: () -> Flow<T>): Flow<T> {
        return flow {
            emitAll(getCurrent().getIfPresent(select, loader))
        }
    }

    override fun getIfPresent(select: SelectQuery): Flow<T> {
        return flow {
            emitAll(getCurrent().getIfPresent(select))
        }
    }

    override suspend fun clear() {
        return getCurrent().clear()
    }

    private suspend fun getCurrent(): NestedQueryStorage<T> {
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
