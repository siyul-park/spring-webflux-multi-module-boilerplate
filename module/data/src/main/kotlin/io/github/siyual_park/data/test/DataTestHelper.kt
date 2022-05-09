package io.github.siyual_park.data.test

import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.data.converter.BytesToULIDConverter
import io.github.siyual_park.data.converter.ULIDToBytesConverter
import io.github.siyual_park.data.migration.MigrationManager
import io.r2dbc.h2.H2ConnectionFactory
import kotlinx.coroutines.CoroutineScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.convert.converter.Converter
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.ReactiveTransaction
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.util.UUID

abstract class DataTestHelper(
    converters: Collection<Converter<*, *>> = emptyList()
) : CoroutineTestHelper() {
    private val database: String = UUID.randomUUID().toString()

    private val connectionFactory = H2ConnectionFactory.inMemory(database)
    val entityOperations = createR2dbcEntityTemplate(
        connectionFactory,
        mutableListOf<Converter<*, *>>(
            ULIDToBytesConverter(),
            BytesToULIDConverter(),
        ).also {
            it.addAll(converters)
        }
    )

    val migrationManager = MigrationManager(entityOperations)

    var transactionManager = R2dbcTransactionManager(connectionFactory)
    var transactionalOperator = TransactionalOperator.create(transactionManager)

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

    fun transactional(func: suspend CoroutineScope.(ReactiveTransaction) -> Unit) = blocking {
        transactionalOperator.executeAndAwait {
            func(it)
        }
    }
}
