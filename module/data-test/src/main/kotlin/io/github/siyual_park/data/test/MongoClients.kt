package io.github.siyual_park.data.test

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.Defaults
import de.flapdoodle.embed.mongo.config.MongoCmdOptions
import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.packageresolver.Command
import de.flapdoodle.embed.process.config.process.ProcessOutput
import de.flapdoodle.embed.process.config.store.DownloadConfig
import de.flapdoodle.embed.process.io.Processors
import de.flapdoodle.embed.process.io.Slf4jLevel
import de.flapdoodle.embed.process.io.progress.Slf4jProgressListener
import de.flapdoodle.embed.process.runtime.Network
import de.flapdoodle.embed.process.store.ExtractedArtifactStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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

private fun getArtifactStore(logger: Logger): ExtractedArtifactStore {
    val downloadConfigBuilder = Defaults.downloadConfigFor(Command.MongoD)
    downloadConfigBuilder.progressListener(Slf4jProgressListener(logger))
    val downloadConfig: DownloadConfig = downloadConfigBuilder.build()
    return Defaults.extractedArtifactStoreFor(Command.MongoD).withDownloadConfig(downloadConfig)
}

fun createEmbeddedMongoDBClients(): Pair<MongodExecutable, MongoClient> {
    val port = Network.getFreeServerPort()
    val mongodConfig = MongodConfig.builder()
        .version(Version.Main.PRODUCTION)
        .cmdOptions(MongoCmdOptions.builder().storageEngine("ephemeralForTest").build())
        .net(Net(port, Network.localhostIsIPv6()))
        .build()

    val mongodExecutable = starter.prepare(mongodConfig)

    return mongodExecutable to MongoClients.create("mongodb://localhost:$port")!!
}
