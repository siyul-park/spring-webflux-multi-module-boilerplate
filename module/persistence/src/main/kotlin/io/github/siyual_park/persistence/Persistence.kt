package io.github.siyual_park.persistence

import io.github.siyual_park.data.repository.Repository
import io.github.siyual_park.data.repository.update
import io.github.siyual_park.data.transaction.SuspendTransactionContextHolder
import kotlinx.coroutines.reactor.mono
import org.springframework.transaction.reactive.TransactionSynchronization
import reactor.core.publisher.Mono
import java.util.Collections

open class Persistence<T : Any, ID : Any>(
    value: T,
    private val repository: Repository<T, ID>,
) {
    protected val root = LazyMutable.from(value)

    private val synchronizations = Collections.synchronizedSet(mutableSetOf<PersistenceSynchronization>())
    private var isCleared = false

    private val synchronization = object : TransactionSynchronization {
        override fun beforeCommit(readOnly: Boolean): Mono<Void> {
            return if (readOnly) {
                Mono.empty()
            } else {
                mono { sync() }.then()
            }
        }
    }

    fun raw(): T {
        return root.raw()
    }

    suspend fun link(): Boolean {
        val context = SuspendTransactionContextHolder.getContext() ?: return false
        val synchronizations = context.synchronizations ?: return false

        synchronizations.add(synchronization)

        return true
    }

    suspend fun clear() {
        if (isCleared) {
            return
        }
        isCleared = true

        try {
            synchronizations.forEach {
                it.beforeClear()
            }

            repository.delete(root.raw())
            root.clear()

            synchronizations.reversed().forEach {
                it.afterClear()
            }
        } catch (exception: Exception) {
            isCleared = false
            throw exception
        }
    }

    suspend fun sync(): Boolean {
        var result = false

        synchronizations.forEach {
            it.beforeSync()
        }

        if (root.isUpdated()) {
            val updated = repository.update(root.raw()) {
                val commands = root.checkout()
                commands.forEach { (property, command) ->
                    property.set(it, command)
                }
                root.raw(it)
            }
            updated?.let { root.raw(it) }
            result = updated != null
        }

        synchronizations.reversed().forEach {
            it.afterSync()
        }

        return result
    }

    protected fun synchronize(synchronization: PersistenceSynchronization) {
        synchronizations.add(synchronization)
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
