package io.github.siyual_park.data.test

import io.github.siyual_park.coroutine.test.CoroutineTest
import io.github.siyual_park.data.converter.BinaryToULIDConverter
import io.github.siyual_park.data.converter.BytesToULIDConverter
import io.github.siyual_park.data.converter.ULIDToBinaryConverter
import io.github.siyual_park.data.converter.ULIDToBytesConverter
import io.github.siyual_park.data.migration.MigrationManager
import io.r2dbc.h2.H2ConnectionFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.convert.converter.Converter
import java.util.UUID

open class MongoTest(
    converters: Collection<Converter<*, *>> = emptyList()
) : CoroutineTest() {
    private val database: String = UUID.randomUUID().toString()

    private val connectionFactory = H2ConnectionFactory.inMemory(database)
    protected val entityOperations = createR2dbcEntityTemplate(
        connectionFactory,
        mutableListOf<Converter<*, *>>(
            ULIDToBytesConverter(),
            BytesToULIDConverter(),
        ).also {
            it.addAll(converters)
        }
    )

    private val mongoClient = createMemMongoClients()
    protected val mongoTemplate = createReactiveMongoTemplate(
        mongoClient, database,
        mutableListOf<Converter<*, *>>(
            BinaryToULIDConverter(),
            ULIDToBinaryConverter(),
        ).also {
            it.addAll(converters)
        }
    )

    protected val migrationManager = MigrationManager(entityOperations)

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