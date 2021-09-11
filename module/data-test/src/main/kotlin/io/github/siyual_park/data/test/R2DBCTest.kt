package io.github.siyual_park.data.test

import io.github.siyual_park.data.migration.MigrationManager
import io.r2dbc.h2.H2ConnectionFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

open class R2DBCTest : CoroutineTest() {
    private val database: String = UUID.randomUUID().toString()
    protected val connectionFactory = H2ConnectionFactory.inMemory(database)
    protected val migrationManager = MigrationManager(connectionFactory)

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
}
