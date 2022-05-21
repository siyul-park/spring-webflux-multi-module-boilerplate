package io.github.siyual_park.data.repository.cache

import kotlinx.coroutines.reactor.mono
import org.springframework.transaction.reactive.TransactionContext
import org.springframework.transaction.reactive.TransactionContextManager
import org.springframework.transaction.reactive.TransactionSynchronization
import reactor.core.publisher.Mono
import java.util.WeakHashMap

class QueryCacheTransactionSynchronization<T : Any> : TransactionSynchronization {
    private val storages = WeakHashMap<TransactionContext, NestedQueryStorage<T>>()

    fun size(): Int {
        return storages.size
    }

    fun get(context: TransactionContext): NestedQueryStorage<T>? {
        return storages[context]
    }

    fun put(context: TransactionContext, defaultValue: NestedQueryStorage<T>) {
        context.synchronizations?.add(this)
        storages[context] = defaultValue
    }

    override fun afterCompletion(status: Int): Mono<Void> {
        return TransactionContextManager.currentContext().flatMap {
            mono { storages.remove(it) }.then()
        }
    }
}
