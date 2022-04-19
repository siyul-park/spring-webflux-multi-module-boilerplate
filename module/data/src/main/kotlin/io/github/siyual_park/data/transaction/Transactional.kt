package io.github.siyual_park.data.transaction

import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.transaction.reactive.TransactionContextManager
import org.springframework.transaction.reactive.TransactionSynchronization
import reactor.core.publisher.Mono

suspend fun doAfterCommit(func: suspend () -> Unit) {
    val context = TransactionContextManager.currentContext().awaitSingleOrNull() ?: return func()
    val synchronizations = context.synchronizations ?: return func()

    synchronizations.add(object : TransactionSynchronization {
        override fun afterCommit(): Mono<Void> {
            return mono { func() }.mapNotNull { null }
        }
    })
}
