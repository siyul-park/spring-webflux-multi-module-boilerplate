package io.github.siyual_park.data.transaction

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.transaction.NoTransactionException
import org.springframework.transaction.reactive.TransactionContext
import org.springframework.transaction.reactive.TransactionContextManager

object SuspendTransactionContextHolder {
    suspend fun getContext(): TransactionContext? {
        return try {
            TransactionContextManager.currentContext().awaitSingleOrNull()
        } catch (e: NoTransactionException) {
            null
        }
    }
}
