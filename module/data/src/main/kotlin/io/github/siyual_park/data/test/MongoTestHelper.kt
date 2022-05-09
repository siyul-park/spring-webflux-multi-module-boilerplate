package io.github.siyual_park.data.test

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.Defaults
import de.flapdoodle.embed.mongo.config.ImmutableMongoCmdOptions
import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.packageresolver.Command
import de.flapdoodle.embed.process.config.process.ProcessOutput
import de.flapdoodle.embed.process.io.Processors
import de.flapdoodle.embed.process.io.Slf4jLevel
import de.flapdoodle.embed.process.io.progress.Slf4jProgressListener
import de.flapdoodle.embed.process.runtime.Network
import de.flapdoodle.embed.process.store.ExtractedArtifactStore
import io.github.siyual_park.data.converter.BinaryToULIDConverter
import io.github.siyual_park.data.converter.ULIDToBinaryConverter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import java.util.UUID

class MongoTestHelper : ResourceTestHelper {
    private val logger = LoggerFactory.getLogger(".EmbeddedMongo")
    private val processOutput = ProcessOutput.builder()
        .output(Processors.logTo(logger, Slf4jLevel.INFO))
        .error(Processors.logTo(logger, Slf4jLevel.ERROR))
        .commands(Processors.named("[console>]", Processors.logTo(logger, Slf4jLevel.DEBUG)))
        .build()
    private val runtimeConfig = Defaults.runtimeConfigFor(Command.MongoD, logger).processOutput(processOutput)
        .artifactStore(getArtifactStore(logger))
        .isDaemonProcess(false).build()

    private val starter = MongodStarter.getInstance(runtimeConfig)

    private lateinit var mongodExecutable: MongodExecutable
    private lateinit var mongoClient: MongoClient
    lateinit var mongoTemplate: ReactiveMongoTemplate

    private val database = UUID.randomUUID().toString()

    override fun setUp() {
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

    override fun tearDown() {
        mongodExecutable.stop()
    }

    private fun getArtifactStore(logger: Logger): ExtractedArtifactStore {
        val downloadConfigBuilder = Defaults.downloadConfigFor(Command.MongoD)
        downloadConfigBuilder.progressListener(Slf4jProgressListener(logger))
        val downloadConfig = downloadConfigBuilder.build()
        return Defaults.extractedArtifactStoreFor(Command.MongoD).withDownloadConfig(downloadConfig)
    }

    private fun createEmbeddedMongoDBClients(): Pair<MongodExecutable, MongoClient> {
        val port = Network.getFreeServerPort()
        val mongodConfig = MongodConfig.builder()
            .version(Version.Main.PRODUCTION)
            .cmdOptions(
                ImmutableMongoCmdOptions.builder()
                    .syncDelay(0)
                    .useSmallFiles(true)
                    .useNoJournal(true)
                    .useNoPrealloc(false)
                    .build()
            )
            .net(Net(port, Network.localhostIsIPv6()))
            .build()

        val mongodExecutable = starter.prepare(mongodConfig)
        return mongodExecutable to MongoClients.create("mongodb://localhost:$port")!!
    }
}
