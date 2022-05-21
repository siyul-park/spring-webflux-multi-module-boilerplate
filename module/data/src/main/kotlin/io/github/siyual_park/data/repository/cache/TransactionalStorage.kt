package io.github.siyual_park.data.repository.cache

import io.github.siyual_park.data.repository.Extractor
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.transaction.NoTransactionException
import org.springframework.transaction.reactive.TransactionContext
import org.springframework.transaction.reactive.TransactionContextManager
import java.util.Stack

class TransactionalStorage<T : Any, ID : Any>(
    private val root: NestedStorage<T, ID>,
) : Storage<T, ID> {
    private val cacheTransactionSynchronization = CacheTransactionSynchronization<NestedStorage<T, ID>>()
    private val mutex = Mutex()

    private val logger = LoggerFactory.getLogger(TransactionalStorage::class.java)

    override val idExtractor: Extractor<T, ID>
        get() = root.idExtractor

    override fun <KEY : Any> createIndex(name: String, extractor: Extractor<T, KEY>) {
        root.createIndex(name, extractor)
    }

    override fun removeIndex(name: String) {
        root.removeIndex(name)
    }

    override fun containsIndex(name: String): Boolean {
        return root.containsIndex(name)
    }

    override fun getExtractors(): Map<String, Extractor<T, *>> {
        return root.getExtractors()
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY): T? {
        return getCurrent().getIfPresent(index, key)
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY, loader: suspend () -> T?): T? {
        return getCurrent().getIfPresent(index, key, loader)
    }

    override suspend fun getIfPresent(id: ID): T? {
        return getCurrent().getIfPresent(id)
    }

    override suspend fun getIfPresent(id: ID, loader: suspend () -> T?): T? {
        return getCurrent().getIfPresent(id, loader)
    }

    override suspend fun remove(id: ID) {
        return getCurrent().remove(id)
    }

    override suspend fun delete(entity: T) {
        return getCurrent().delete(entity)
    }

    override suspend fun put(entity: T) {
        return getCurrent().put(entity)
    }

    override suspend fun clear() {
        return getCurrent().clear()
    }

    private suspend fun getCurrent(): NestedStorage<T, ID> {
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
