package io.github.siyual_park.data.configuration

import com.mongodb.MongoClientSettings
import de.flapdoodle.embed.mongo.Command
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.Defaults
import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.process.config.RuntimeConfig
import de.flapdoodle.embed.process.config.io.ProcessOutput
import de.flapdoodle.embed.process.config.store.DownloadConfig
import de.flapdoodle.embed.process.io.Processors
import de.flapdoodle.embed.process.io.Slf4jLevel
import de.flapdoodle.embed.process.io.progress.Slf4jProgressListener
import de.flapdoodle.embed.process.store.ExtractedArtifactStore
import io.github.siyual_park.data.property.EmbeddedMongoCustomProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.embedded.DownloadConfigBuilderCustomizer
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.stream.Stream

@Suppress("UNCHECKED_CAST")
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MongoProperties::class, EmbeddedMongoProperties::class)
@AutoConfigureBefore(MongoAutoConfiguration::class, MongoReactiveAutoConfiguration::class)
@ConditionalOnClass(MongoClientSettings::class, MongodStarter::class)
class EmbeddedMongoAutoConfiguration(
    mongoProperties: MongoProperties,
    private val embeddedMongoCustomProperties: EmbeddedMongoCustomProperties
) {
    private val embeddedMongoAutoConfiguration = EmbeddedMongoAutoConfiguration(mongoProperties)

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    fun embeddedMongoServer(
        mongodConfig: MongodConfig?,
        runtimeConfig: RuntimeConfig?,
        context: ApplicationContext?
    ): MongodExecutable? {
        if (!embeddedMongoCustomProperties.enable || mongodConfig == null) {
            return null
        }

        return embeddedMongoAutoConfiguration.embeddedMongoServer(mongodConfig, runtimeConfig, context)
    }

    @Bean
    @ConditionalOnMissingBean
    fun embeddedMongoConfiguration(embeddedProperties: EmbeddedMongoProperties): MongodConfig? {
        if (!embeddedMongoCustomProperties.enable) {
            return null
        }

        return embeddedMongoAutoConfiguration.embeddedMongoConfiguration(embeddedProperties)
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(Logger::class)
    @ConditionalOnMissingBean(RuntimeConfig::class)
    internal class RuntimeConfigConfiguration(
        private val embeddedMongoCustomProperties: EmbeddedMongoCustomProperties
    ) {
        @Bean
        fun embeddedMongoRuntimeConfig(
            downloadConfigBuilderCustomizers: ObjectProvider<DownloadConfigBuilderCustomizer>
        ): RuntimeConfig? {
            if (!embeddedMongoCustomProperties.enable) {
                return null
            }

            val logger = LoggerFactory.getLogger(javaClass.getPackage().name + ".EmbeddedMongo")
            val processOutput = ProcessOutput(
                Processors.logTo(logger, Slf4jLevel.INFO),
                Processors.logTo(logger, Slf4jLevel.ERROR),
                Processors.named("[console>]", Processors.logTo(logger, Slf4jLevel.DEBUG))
            )
            return Defaults.runtimeConfigFor(Command.MongoD, logger).processOutput(processOutput)
                .artifactStore(getArtifactStore(logger, downloadConfigBuilderCustomizers.orderedStream()))
                .isDaemonProcess(false).build()
        }

        private fun getArtifactStore(
            logger: Logger,
            downloadConfigBuilderCustomizers: Stream<DownloadConfigBuilderCustomizer>
        ): ExtractedArtifactStore {
            val downloadConfigBuilder = Defaults.downloadConfigFor(Command.MongoD)
            downloadConfigBuilder.progressListener(Slf4jProgressListener(logger))
            downloadConfigBuilderCustomizers.forEach { customizer: DownloadConfigBuilderCustomizer ->
                customizer.customize(
                    downloadConfigBuilder
                )
            }
            val downloadConfig: DownloadConfig = downloadConfigBuilder.build()
            return Defaults.extractedArtifactStoreFor(Command.MongoD).withDownloadConfig(downloadConfig)
        }
    }
}