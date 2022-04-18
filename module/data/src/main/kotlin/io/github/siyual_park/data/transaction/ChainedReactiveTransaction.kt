package io.github.siyual_park.data.transaction

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.transaction.ReactiveTransaction
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.TransactionDefinition
import java.util.Collections

class ChainedReactiveTransaction(
    val mainTransactionManager: ReactiveTransactionManager
) : ReactiveTransaction {
    val transactionStatuses = Collections
        .synchronizedMap(mutableMapOf<ReactiveTransactionManager, ReactiveTransaction>())

    suspend fun registerTransactionManager(
        definition: TransactionDefinition?,
        transactionManager: ReactiveTransactionManager
    ) {
        transactionStatuses[transactionManager] = transactionManager.getReactiveTransaction(definition).awaitSingle()
    }

    fun getTransaction(transactionManager: ReactiveTransactionManager): ReactiveTransaction? {
        return transactionStatuses[transactionManager]
    }

    override fun isNewTransaction(): Boolean {
        return getMainTranslation().isNewTransaction
    }

    override fun setRollbackOnly() {
        transactionStatuses.values.forEach { it.setRollbackOnly() }
    }

    override fun isRollbackOnly(): Boolean {
        return getMainTranslation().isRollbackOnly
    }

    override fun isCompleted(): Boolean {
        return getMainTranslation().isCompleted
    }

    private fun getMainTranslation(): ReactiveTransaction {
        return transactionStatuses[mainTransactionManager]!!
    }
}
