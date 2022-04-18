package io.github.siyual_park.data.test

import com.mongodb.reactivestreams.client.MongoClient
import de.flapdoodle.embed.mongo.MongodExecutable
import io.github.siyual_park.coroutine.test.CoroutineTest
import io.github.siyual_park.data.converter.BinaryToULIDConverter
import io.github.siyual_park.data.converter.BytesToULIDConverter
import io.github.siyual_park.data.converter.ULIDToBinaryConverter
import io.github.siyual_park.data.converter.ULIDToBytesConverter
import io.github.siyual_park.data.migration.MigrationManager
import io.r2dbc.h2.H2ConnectionFactory
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import java.util.UUID

open class MongoTest(
    converters: Collection<Converter<*, *>> = emptyList()
) : CoroutineTest() {
    private val database: String = UUID.randomUUID().toString()

    private val connectionFactory = H2ConnectionFactory.inMemory(database)
    private val entityOperations = createR2dbcEntityTemplate(
        connectionFactory,
        mutableListOf<Converter<*, *>>(
            ULIDToBytesConverter(),
            BytesToULIDConverter(),
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

    companion object {
        lateinit var mongodExecutable: MongodExecutable
        lateinit var mongoClient: MongoClient
        lateinit var mongoTemplate: ReactiveMongoTemplate

        private val database = UUID.randomUUID().toString()

        @BeforeAll
        @JvmStatic
        fun setupMongo() {
            val mongodExecutableAndClient = createEmbeddedMongoDBClients()
            mongodExecutable = mongodExecutableAndClient.first
            mongodExecutable.start()

            mongoClient = mongodExecutableAndClient.second
            mongoTemplate = createReactiveMongoTemplate(
                mongoClient,
                database,
                mutableListOf<Converter<*, *>>(
                    BinaryToULIDConverter(),
                    ULIDToBinaryConverter(),
                )
            )
        }

        @AfterAll
        @JvmStatic
        fun stopMongo() {
            mongodExecutable.stop()
        }
    }
}
