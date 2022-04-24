package io.github.siyual_park.data.transaction

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.transaction.NoTransactionException
import org.springframework.transaction.reactive.TransactionContext
import org.springframework.transaction.reactive.TransactionContextManager

suspend fun currentContextOrNull(): TransactionContext? {
    try {
        return TransactionContextManager.currentContext().awaitSingleOrNull()
    } catch (e: NoTransactionException) {
        return null
    }
}
