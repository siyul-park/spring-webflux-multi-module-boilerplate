package io.github.siyual_park.data.test

import com.mongodb.reactivestreams.client.MongoClients
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import io.github.siyual_park.coroutine.test.CoroutineTest
import io.github.siyual_park.data.migration.MigrationManager
import io.github.siyual_park.ulid.converter.BytesToULIDConverter
import io.github.siyual_park.ulid.converter.ULIDToBytesConverter
import io.r2dbc.h2.H2ConnectionFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.dialect.DialectResolver
import org.springframework.r2dbc.core.DatabaseClient
import java.util.UUID

open class MongoTest(
    converter: Collection<Converter<*, *>> = emptyList()
) : CoroutineTest() {
    private val database: String = UUID.randomUUID().toString()

    protected val connectionFactory = H2ConnectionFactory.inMemory(database)
    protected val dialect = DialectResolver.getDialect(connectionFactory)
    protected val databaseClient = DatabaseClient.builder().connectionFactory(connectionFactory).bindMarkers(dialect.bindMarkersFactory).build()

    protected val r2dbcConverter = DefaultReactiveDataAccessStrategy.createConverter(
        dialect,
        mutableListOf<Converter<*, *>>(
            ULIDToBytesConverter(),
            BytesToULIDConverter()
        ).also {
            it.addAll(converter)
        }
    )

    protected val entityOperations = R2dbcEntityTemplate(
        databaseClient,
        dialect,
        r2dbcConverter
    )
    protected val migrationManager = MigrationManager(entityOperations)

    private val starter = MongodStarter.getDefaultInstance()

    private val port = Network.getFreeServerPort()
    private val mongodConfig = MongodConfig.builder()
        .version(Version.Main.PRODUCTION)
        .net(Net(port, Network.localhostIsIPv6()))
        .build()

    private val mongodExecutable = starter.prepare(mongodConfig)
    private val mongodProcess = mongodExecutable.start()

    private val mongoClient = MongoClients.create("mongodb://localhost:$port")

    protected val mongoTemplate = ReactiveMongoTemplate(mongoClient, database)

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
