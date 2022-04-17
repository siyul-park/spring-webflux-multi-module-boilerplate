package io.github.siyual_park.data.test

import com.mongodb.reactivestreams.client.MongoClients
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
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
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.dialect.DialectResolver
import org.springframework.r2dbc.core.DatabaseClient
import java.util.UUID

open class MongoTest(
    converters: Collection<Converter<*, *>> = emptyList()
) : CoroutineTest() {
    private val database: String = UUID.randomUUID().toString()

    private val connectionFactory = H2ConnectionFactory.inMemory(database)
    private val dialect = DialectResolver.getDialect(connectionFactory)
    private val databaseClient = DatabaseClient.builder().connectionFactory(connectionFactory).bindMarkers(dialect.bindMarkersFactory).build()

    private val r2dbcConverter = DefaultReactiveDataAccessStrategy.createConverter(
        dialect,
        mutableListOf<Converter<*, *>>(
            ULIDToBytesConverter(),
            BytesToULIDConverter(),
        ).also {
            it.addAll(converters)
        }
    )

    private val entityOperations = R2dbcEntityTemplate(
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

    private val mongoDatabaseFactory = SimpleReactiveMongoDatabaseFactory(mongoClient, database)

    private val mongoConverter = run {
        val conversions = MongoCustomConversions(
            mutableListOf<Converter<*, *>>(
                BinaryToULIDConverter(),
                ULIDToBinaryConverter(),
            ).also {
                it.addAll(converters)
            }
        )
        val context = MongoMappingContext()
        context.setSimpleTypeHolder(conversions.simpleTypeHolder)
        context.afterPropertiesSet()
        val converter = MappingMongoConverter(ReactiveMongoTemplate.NO_OP_REF_RESOLVER, context)
        converter.setCustomConversions(conversions)
        converter.setCodecRegistryProvider(mongoDatabaseFactory)
        converter.afterPropertiesSet()
        converter
    }

    protected val mongoTemplate = ReactiveMongoTemplate(mongoDatabaseFactory, mongoConverter)

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
