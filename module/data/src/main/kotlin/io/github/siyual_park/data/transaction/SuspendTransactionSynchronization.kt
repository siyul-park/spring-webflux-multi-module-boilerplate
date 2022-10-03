package io.github.siyual_park.data.transaction

import kotlinx.coroutines.reactor.mono
import org.springframework.transaction.reactive.TransactionSynchronization
import reactor.core.publisher.Mono

object SuspendTransactionSynchronization {
    suspend fun doAfterCommit(func: suspend () -> Unit) {
        val synchronizations = SuspendTransactionContextHolder.getContext()?.synchronizations ?: return func()

        synchronizations.add(object : TransactionSynchronization {
            override fun afterCommit(): Mono<Void> {
                return mono { func() }.mapNotNull { null }
            }
        })
    }
}
