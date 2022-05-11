package io.github.siyual_park.persistence

import io.github.siyual_park.data.event.AfterDeleteEvent
import io.github.siyual_park.data.event.AfterUpdateEvent
import io.github.siyual_park.data.event.BeforeDeleteEvent
import io.github.siyual_park.data.event.BeforeUpdateEvent
import io.github.siyual_park.data.repository.Repository
import io.github.siyual_park.data.repository.update
import io.github.siyual_park.data.transaction.currentContextOrNull
import io.github.siyual_park.event.EventPublisher
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.transaction.reactive.TransactionSynchronization
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import reactor.core.publisher.Mono
import java.util.Collections

open class Persistence<T : Any, ID : Any>(
    value: T,
    private val repository: Repository<T, ID>,
    private val operator: TransactionalOperator? = null,
    private val eventPublisher: EventPublisher? = null,
) : Permanentable {
    protected val root = LazyMutable.from(value)

    private val synchronizations = Collections.synchronizedSet(mutableSetOf<PersistenceSynchronization>())
    private val mutex = Mutex()
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

    fun synchronize(synchronization: PersistenceSynchronization) {
        synchronizations.add(synchronization)
    }

    override suspend fun link(): Boolean {
        val context = currentContextOrNull() ?: return false
        val synchronizations = context.synchronizations ?: return false

        synchronizations.add(synchronization)

        return true
    }

    override suspend fun clear() {
        if (isCleared) {
            return
        }

        mutex.withLock {
            if (isCleared) {
                return@withLock
            }
            isCleared = true
            try {
                withTransaction {
                    synchronizations.forEach {
                        it.beforeClear()
                    }
                    eventPublisher?.publish(BeforeDeleteEvent(this))
                    runClear()
                    eventPublisher?.publish(AfterDeleteEvent(this))
                    synchronizations.forEach {
                        it.afterClear()
                    }
                }
            } catch (exception: Exception) {
                isCleared = false
                throw exception
            }
        }
    }

    protected open suspend fun runClear() {
        repository.delete(root.raw())
        root.clear()
    }

    override suspend fun sync(): Boolean {
        var result = false
        if (!root.isUpdated()) {
            return result
        }
        mutex.withLock {
            if (!root.isUpdated()) {
               return@withLock
            }
            withTransaction {
                synchronizations.forEach {
                    it.beforeSync()
                }
                eventPublisher?.publish(BeforeUpdateEvent(this))
                result = runSync()
                eventPublisher?.publish(AfterUpdateEvent(this))
                synchronizations.forEach {
                    it.afterSync()
                }
            }
        }

        return result
    }

    protected open suspend fun runSync(): Boolean {
        val updated = repository.update(root.raw()) {
            val commands = root.checkout()
            commands.forEach { (property, command) ->
                property.set(it, command)
            }
            root.raw(it)
        }
        updated?.let { root.raw(it) }
        return updated != null
    }

    private suspend fun <U : Any> withTransaction(func: suspend () -> U): U? {
        return if (operator == null) {
            func()
        } else {
            operator.executeAndAwait { func() }
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
