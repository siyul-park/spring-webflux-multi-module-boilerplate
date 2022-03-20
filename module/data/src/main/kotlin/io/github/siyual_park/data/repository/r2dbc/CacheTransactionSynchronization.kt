package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.repository.cache.NestedStorage
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.transaction.reactive.TransactionContext
import org.springframework.transaction.reactive.TransactionContextManager
import org.springframework.transaction.reactive.TransactionSynchronization
import org.springframework.transaction.reactive.TransactionSynchronization.STATUS_COMMITTED
import reactor.core.publisher.Mono
import java.util.WeakHashMap

class CacheTransactionSynchronization<T : Any, ID : Any> : TransactionSynchronization {
    private val storages = WeakHashMap<TransactionContext, NestedStorage<T, ID>>()
    private val logger = LoggerFactory.getLogger(CacheTransactionSynchronization::class.java)

    fun size(): Int {
        return storages.size
    }

    fun get(context: TransactionContext): NestedStorage<T, ID>? {
        return storages[context]
    }

    fun getOrPut(context: TransactionContext, defaultValue: () -> NestedStorage<T, ID>): NestedStorage<T, ID> {
        return storages.getOrPut(context, defaultValue)
    }

    override fun afterCompletion(status: Int): Mono<Void> {
        return TransactionContextManager.currentContext().flatMap {
            if (status == STATUS_COMMITTED) {
                mono {
                    val storage = storages[it]
                    if (storage != null) {
                        val diff = storage.diff()
                        val parent = storage.parent

                        logger.debug("Merging Cache Storage [parent: $parent, child: $storage, diff: $diff]")

                        parent?.merge(storage)
                    }
                }.then()
            } else {
                mono {
                    val storage = storages[it]
                    if (storage != null) {
                        val diff = storage.diff()
                        val parent = storage.parent
                        logger.debug("Removing Cache Storage [parent: $parent, child: $storage, diff: $diff]")
                        storage.clear()
                    }
                    storages.remove(it)
                }.then()
            }
        }
    }
}
