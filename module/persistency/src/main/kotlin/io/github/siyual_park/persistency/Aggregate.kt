package io.github.siyual_park.persistency

import io.github.siyual_park.data.repository.Repository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.transaction.reactive.TransactionContextManager
import org.springframework.transaction.reactive.TransactionSynchronization
import reactor.core.publisher.Mono

open class Aggregate<T : Any, ID : Any>(
    value: T,
    protected val repository: Repository<T, ID>
) {
    protected val root = LazyMutable.from(value)

    private val synchronization = object : TransactionSynchronization {
        override fun beforeCommit(readOnly: Boolean): Mono<Void> {
            return if (readOnly) {
                Mono.empty()
            } else {
                mono {
                    if (root.isUpdated()) {
                        sync()
                    }
                }.then()
            }
        }
    }

    open suspend fun link(): Boolean {
        val context = TransactionContextManager.currentContext().awaitSingleOrNull() ?: return false
        val synchronizations = context.synchronizations ?: return false

        synchronizations.add(synchronization)

        return true
    }

    open suspend fun clear() {
        repository.delete(root.getValue())
        root.clear()
    }

    open suspend fun sync(): Boolean {
        return repository.update(root.getValue(), root.toPatch()) != null
    }
}
