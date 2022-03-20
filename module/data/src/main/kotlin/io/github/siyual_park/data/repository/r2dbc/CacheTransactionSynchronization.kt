package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.repository.cache.NestedStorage
import kotlinx.coroutines.reactor.mono
import org.springframework.transaction.reactive.TransactionContext
import org.springframework.transaction.reactive.TransactionContextManager
import org.springframework.transaction.reactive.TransactionSynchronization
import org.springframework.transaction.reactive.TransactionSynchronization.STATUS_COMMITTED
import reactor.core.publisher.Mono
import java.util.WeakHashMap

class CacheTransactionSynchronization<T : Any, ID : Any> : TransactionSynchronization {
    private val storages = WeakHashMap<TransactionContext, NestedStorage<T, ID>>()

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
                        val parent = storage.parent
                        parent?.join(storage)
                    }
                }.then()
            } else {
                mono {
                    storages[it]?.clear()
                    storages.remove(it)
                }.then()
            }
        }
    }
}
