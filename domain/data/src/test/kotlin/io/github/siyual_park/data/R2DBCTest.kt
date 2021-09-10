package io.github.siyual_park.data

import io.github.siyual_park.data.migration.MigrationManager
import io.r2dbc.h2.H2ConnectionFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import java.util.*

open class R2DBCTest: CoroutineTest() {
    private val database: String = UUID.randomUUID().toString()
    protected val connectionFactory = H2ConnectionFactory.inMemory(database)
    protected val migrationManager = MigrationManager(connectionFactory)

    @BeforeEach
    override fun setUp() {
        super.setUp()

        async {
            migrationManager.run()
        }
    }

    @AfterEach
    override fun tearDown() {
        async {
            migrationManager.revert()
        }

        super.tearDown()
    }

    @Test
    fun contextLoads() {
    }
}