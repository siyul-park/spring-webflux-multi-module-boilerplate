package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.repository.cache.NestedStorage
import kotlinx.coroutines.reactor.mono
import org.springframework.transaction.reactive.TransactionContextManager
import org.springframework.transaction.reactive.TransactionSynchronization
import reactor.core.publisher.Mono

class CacheTransactionSynchronization<T : Any, ID : Any>(
    val storage: NestedStorage<T, ID>
) : TransactionSynchronization {

    override fun afterCommit(): Mono<Void> {
        return TransactionContextManager.currentContext().flatMap {
            mono {
                val parent = storage.parent
                parent?.join(storage)
            }
        }.then()
    }
}
