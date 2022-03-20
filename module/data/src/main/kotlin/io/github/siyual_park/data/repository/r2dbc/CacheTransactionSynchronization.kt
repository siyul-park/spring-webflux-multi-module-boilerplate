package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.repository.cache.NestedStorage
import kotlinx.coroutines.reactor.mono
import org.springframework.transaction.reactive.TransactionContext
import org.springframework.transaction.reactive.TransactionContextManager
import org.springframework.transaction.reactive.TransactionSynchronization
import reactor.core.publisher.Mono
import java.util.WeakHashMap

class CacheTransactionSynchronization<T : Any, ID : Any> : TransactionSynchronization {
    private val storages = WeakHashMap<TransactionContext, NestedStorage<T, ID>>()

    fun get(context: TransactionContext): NestedStorage<T, ID>? {
        return storages[context]
    }

    fun getOrPut(context: TransactionContext, defaultValue: () -> NestedStorage<T, ID>): NestedStorage<T, ID> {
        return storages.getOrPut(context, defaultValue)
    }

    override fun afterCommit(): Mono<Void> {
        return TransactionContextManager.currentContext().flatMap {
            mono {
                val storage = storages[it]
                if (storage != null) {
                    val parent = storage.parent
                    parent?.join(storage)
                }
            }
        }.then()
    }
}
