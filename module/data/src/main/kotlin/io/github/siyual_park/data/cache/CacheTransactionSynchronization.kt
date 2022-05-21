package io.github.siyual_park.data.cache

import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.transaction.reactive.TransactionContext
import org.springframework.transaction.reactive.TransactionContextManager
import org.springframework.transaction.reactive.TransactionSynchronization
import org.springframework.transaction.reactive.TransactionSynchronization.STATUS_COMMITTED
import reactor.core.publisher.Mono
import java.util.WeakHashMap

class CacheTransactionSynchronization<S : GeneralNestedStorage<S>> : TransactionSynchronization {
    private val storages = WeakHashMap<TransactionContext, S>()
    private val logger = LoggerFactory.getLogger(CacheTransactionSynchronization::class.java)

    fun size(): Int {
        return storages.size
    }

    fun get(context: TransactionContext): S? {
        return storages[context]
    }

    fun put(context: TransactionContext, defaultValue: S) {
        context.synchronizations?.add(this)
        storages[context] = defaultValue
    }

    override fun afterCompletion(status: Int): Mono<Void> {
        return TransactionContextManager.currentContext().flatMap {
            if (status == STATUS_COMMITTED) {
                mono {
                    val storage = storages[it]
                    val parent = storage?.parent
                    if (storage?.parent != null) {
                        logger.debug("Merging Cache Storage [parent: $parent, child: $storage]")
                        parent?.merge(storage)
                    }
                    storages.remove(it)
                }.then()
            } else {
                mono {
                    val storage = storages[it]
                    if (storage != null) {
                        logger.debug("Removing Cache Storage $storage")
                        storage.clear()
                    }
                    storages.remove(it)
                }.then()
            }
        }
    }
}
