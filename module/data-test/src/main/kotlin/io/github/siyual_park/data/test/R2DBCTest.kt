package io.github.siyual_park.data.test

import io.github.siyual_park.data.migration.MigrationManager
import io.github.siyual_park.spring.test.CoroutineTest
import io.r2dbc.h2.H2ConnectionFactory
import kotlinx.coroutines.CoroutineScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.util.UUID

open class R2DBCTest : CoroutineTest() {
    private val database: String = UUID.randomUUID().toString()

    protected val connectionFactory = H2ConnectionFactory.inMemory(database)
    protected val entityOperations = R2dbcEntityTemplate(connectionFactory)
    protected val migrationManager = MigrationManager(entityOperations)

    protected var transactionManager = R2dbcTransactionManager(connectionFactory)
    protected var transactionalOperator = TransactionalOperator.create(transactionManager)

    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            migrationManager.run()
        }
    }

    @AfterEach
    override fun tearDown() {
        blocking {
            migrationManager.revert()
        }

        super.tearDown()
    }

    @Test
    fun contextLoads() {
    }

    fun transactional(func: suspend CoroutineScope.() -> Unit) = blocking {
        transactionalOperator.executeAndAwait {
            func()
        }
    }
}
