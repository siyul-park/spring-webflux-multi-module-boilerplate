package io.github.siyual_park.persistence

import io.github.siyual_park.data.event.AfterDeleteEvent
import io.github.siyual_park.data.event.AfterUpdateEvent
import io.github.siyual_park.data.event.BeforeDeleteEvent
import io.github.siyual_park.data.event.BeforeUpdateEvent
import io.github.siyual_park.data.repository.Repository
import io.github.siyual_park.data.repository.update
import io.github.siyual_park.event.EventPublisher
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.sync.Semaphore
import org.springframework.transaction.NoTransactionException
import org.springframework.transaction.reactive.TransactionContextManager
import org.springframework.transaction.reactive.TransactionSynchronization
import reactor.core.publisher.Mono
import java.util.Collections
import java.util.concurrent.ConcurrentLinkedQueue

open class Persistence<T : Any, ID : Any>(
    value: T,
    private val repository: Repository<T, ID>,
    private val eventPublisher: EventPublisher? = null
) : Permanentable {
    protected val root = LazyMutable.from(value)

    private val beforeSyncTmpJobs = ConcurrentLinkedQueue<suspend () -> Unit>()
    private val afterSyncTmpJobs = ConcurrentLinkedQueue<suspend () -> Unit>()

    private val beforeSyncJobs = Collections.synchronizedList(mutableListOf<suspend () -> Unit>())
    private val afterSyncJobs = Collections.synchronizedList(mutableListOf<suspend () -> Unit>())

    private val semaphore = Semaphore(1)
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
        doClear {
            eventPublisher?.publish(BeforeDeleteEvent(this))
            repository.delete(root.raw())
            root.clear()
            eventPublisher?.publish(AfterDeleteEvent(this))
            isCleared = true
        }
    }

    override suspend fun sync(): Boolean {
        return doSync {
            eventPublisher?.publish(BeforeUpdateEvent(this))
            val updated = repository.update(root.raw()) {
                val commands = root.checkout()
                commands.forEach { (property, command) ->
                    property.set(it, command)
                }
                root.raw(it)
            }
            eventPublisher?.publish(AfterUpdateEvent(this))

            updated != null
        }
    }

    protected suspend fun doClear(job: suspend () -> Unit) {
        if (!isCleared) {
            semaphore.acquire()
            try {
                if (isCleared) {
                    return
                }
                job.invoke()
            } finally {
                semaphore.release()
            }
        }
    }

    protected suspend fun doSync(job: suspend () -> Boolean): Boolean {
        beforeSyncJobs.forEach {
            it.invoke()
        }

        return if (root.isUpdated() || beforeSyncTmpJobs.isNotEmpty() || afterSyncTmpJobs.isNotEmpty()) {
            semaphore.acquire()
            try {
                while (beforeSyncTmpJobs.isNotEmpty()) {
                    beforeSyncTmpJobs.poll().invoke()
                }

                if (!root.isUpdated()) {
                    return false
                }
                val result = job.invoke()

                while (afterSyncTmpJobs.isNotEmpty()) {
                    afterSyncTmpJobs.poll().invoke()
                }

                result
            } finally {
                semaphore.release()
            }
        } else {
            false
        }.also {
            afterSyncJobs.forEach {
                it.invoke()
            }
        }
    }

    protected fun onBeforeSyncOne(job: suspend () -> Unit) {
        this.beforeSyncTmpJobs.add(job)
    }

    protected fun onAfterSyncOne(job: suspend () -> Unit) {
        this.beforeSyncTmpJobs.add(job)
    }

    protected fun onBeforeSync(job: suspend () -> Unit) {
        this.beforeSyncJobs.add(job)
    }

    protected fun onAfterSync(job: suspend () -> Unit) {
        this.beforeSyncJobs.add(job)
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
