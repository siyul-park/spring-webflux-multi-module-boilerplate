package io.github.siyual_park.persistence

import io.github.siyual_park.data.repository.Repository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.transaction.NoTransactionException
import org.springframework.transaction.reactive.TransactionContextManager
import org.springframework.transaction.reactive.TransactionSynchronization
import reactor.core.publisher.Mono

open class Persistence<T : Any, ID : Any>(
    value: T,
    private val repository: Repository<T, ID>
) : Permanentable {
    protected val root = LazyMutable.from(value)

    private val synchronization = object : TransactionSynchronization {
        override fun beforeCommit(readOnly: Boolean): Mono<Void> {
            return if (readOnly) {
                Mono.empty()
            } else {
                mono {
                    sync()
                }.then()
            }
        }
    }

    fun raw(): T {
        return root.raw()
    }

    override suspend fun link(): Boolean {
        try {
            val context = TransactionContextManager.currentContext().awaitSingleOrNull() ?: return false
            val synchronizations = context.synchronizations ?: return false

            synchronizations.add(synchronization)

            return true
        } catch (e: NoTransactionException) {
            return false
        }
    }

    override suspend fun clear() {
        repository.delete(root.raw())
        root.clear()
    }

    override suspend fun sync(): Boolean {
        return if (root.isUpdated()) {
            repository.update(root.raw(), root.toPatch()) != null
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return root.raw().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Persistence<*, *>) {
            return false
        }

        return raw() == other.raw()
    }

    override fun toString(): String {
        return root.raw().toString()
    }
}
