package io.github.siyual_park.data.transaction

import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.transaction.CannotCreateTransactionException
import org.springframework.transaction.HeuristicCompletionException
import org.springframework.transaction.ReactiveTransaction
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.UnexpectedRollbackException
import reactor.core.publisher.Mono
import java.util.Collections

class ReactiveChainedTransactionManager : ReactiveTransactionManager {
    private val logger = LoggerFactory.getLogger(ReactiveChainedTransactionManager::class.java)

    private val transactionManagers = Collections.synchronizedList(mutableListOf<ReactiveTransactionManager>())

    fun registerTransactionManager(transactionManager: ReactiveTransactionManager) {
        transactionManagers.add(transactionManager)
    }

    override fun getReactiveTransaction(definition: TransactionDefinition?): Mono<ReactiveTransaction> {
        return mono {
            val chainedTransaction = ChainedReactiveTransaction(transactionManagers[0])

            if (definition == null) {
                chainedTransaction
            } else {
                try {
                    for (transactionManager in transactionManagers) {
                        chainedTransaction.registerTransactionManager(definition, transactionManager)
                    }
                } catch (ex: Exception) {
                    for (transactionManager in transactionManagers) {
                        try {
                            chainedTransaction.getTransaction(transactionManager)?.let {
                                transactionManager.rollback(it).awaitSingleOrNull()
                            }
                        } catch (ex2: Exception) {
                            logger.warn("Rollback exception (" + transactionManager + ") " + ex2.message, ex2)
                        }
                    }
                    throw CannotCreateTransactionException(ex.message ?: "", ex)
                }

                chainedTransaction
            }
        }
    }

    override fun commit(transaction: ReactiveTransaction): Mono<Void> {
        return mono {
            val chainedTransaction = transaction as ChainedReactiveTransaction

            var commit = true
            var commitException: Exception? = null
            var commitExceptionTransactionManager: ReactiveTransactionManager? = null

            for (transactionManager in transactionManagers.reversed()) {
                if (commit) {
                    try {
                        chainedTransaction.getTransaction(transactionManager)?.let {
                            transactionManager.commit(it).awaitSingleOrNull()
                        }
                    } catch (ex: Exception) {
                        commit = false
                        commitException = ex
                        commitExceptionTransactionManager = transactionManager
                    }
                } else {
                    try {
                        chainedTransaction.getTransaction(transactionManager)?.let {
                            transactionManager.rollback(it).awaitSingleOrNull()
                        }
                    } catch (ex: java.lang.Exception) {
                        logger.warn("Rollback exception (after commit) (" + transactionManager + ") " + ex.message, ex)
                    }
                }
            }

            if (commitException != null) {
                val firstTransactionManagerFailed = commitExceptionTransactionManager === getLastTransactionManager()
                val transactionState = if (firstTransactionManagerFailed) {
                    HeuristicCompletionException.STATE_ROLLED_BACK
                } else {
                    HeuristicCompletionException.STATE_MIXED
                }
                throw HeuristicCompletionException(transactionState, commitException)
            }
        }.mapNotNull { null }
    }

    override fun rollback(transaction: ReactiveTransaction): Mono<Void> {
        return mono {
            var rollbackException: Exception? = null
            var rollbackExceptionTransactionManager: ReactiveTransactionManager? = null

            val chainedTransaction = transaction as ChainedReactiveTransaction

            for (transactionManager in transactionManagers.reversed()) {
                try {
                    chainedTransaction.getTransaction(transactionManager)?.let {
                        transactionManager.rollback(it).awaitSingleOrNull()
                    }
                } catch (ex: java.lang.Exception) {
                    if (rollbackException == null) {
                        rollbackException = ex
                        rollbackExceptionTransactionManager = transactionManager
                    } else {
                        logger.warn("Rollback exception (" + transactionManager + ") " + ex.message, ex)
                    }
                }
            }

            if (rollbackException != null) {
                throw UnexpectedRollbackException(
                    "Rollback exception, originated at (" + rollbackExceptionTransactionManager + ") " + rollbackException.message, rollbackException
                )
            }
        }.mapNotNull { null }
    }

    private fun getLastTransactionManager(): ReactiveTransactionManager {
        return transactionManagers[lastTransactionManagerIndex()]
    }

    private fun lastTransactionManagerIndex(): Int {
        return transactionManagers.size - 1
    }
}
