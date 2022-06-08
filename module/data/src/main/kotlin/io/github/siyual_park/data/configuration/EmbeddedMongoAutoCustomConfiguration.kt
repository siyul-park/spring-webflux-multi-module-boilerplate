package io.github.siyual_park.data.configuration

import com.mongodb.MongoClientSettings
import com.mongodb.reactivestreams.client.MongoClient
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.Defaults
import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.packageresolver.Command
import de.flapdoodle.embed.process.config.RuntimeConfig
import de.flapdoodle.embed.process.config.process.ProcessOutput
import de.flapdoodle.embed.process.io.Processors
import de.flapdoodle.embed.process.io.Slf4jLevel
import de.flapdoodle.embed.process.io.progress.Slf4jProgressListener
import de.flapdoodle.embed.process.store.ExtractedArtifactStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.data.mongo.ReactiveStreamsMongoClientDependsOnBeanFactoryPostProcessor
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
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.ReactiveMongoClientFactoryBean
import java.util.stream.Stream

@Suppress("UNCHECKED_CAST")
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MongoProperties::class, EmbeddedMongoProperties::class)
@AutoConfigureBefore(MongoAutoConfiguration::class, MongoReactiveAutoConfiguration::class)
@ConditionalOnClass(MongoClientSettings::class, MongodStarter::class)
@ConditionalOnProperty(name = ["spring.mongodb.embedded.enable"], havingValue = "true")
@Import(
    EmbeddedMongoAutoCustomConfiguration.ReactiveStreamsDependsOnBeanFactoryPostProcessor::class
)

class EmbeddedMongoAutoCustomConfiguration(
    mongoProperties: MongoProperties,
) {
    private val embeddedMongoAutoConfiguration = EmbeddedMongoAutoConfiguration(mongoProperties)

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    fun embeddedMongoServer(
        mongodConfig: MongodConfig?,
        runtimeConfig: RuntimeConfig?,
        context: ApplicationContext?
    ): MongodExecutable? {
        return embeddedMongoAutoConfiguration.embeddedMongoServer(mongodConfig, runtimeConfig, context)
    }

    @Bean
    @ConditionalOnMissingBean
    fun embeddedMongoConfiguration(embeddedProperties: EmbeddedMongoProperties): MongodConfig? {
        return embeddedMongoAutoConfiguration.embeddedMongoConfiguration(embeddedProperties)
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(Logger::class)
    @ConditionalOnMissingBean(RuntimeConfig::class)
    @ConditionalOnProperty(name = ["spring.mongodb.embedded.enable"], havingValue = "true")
    internal class RuntimeConfigConfiguration {
        @Bean
        fun embeddedMongoRuntimeConfig(
            downloadConfigBuilderCustomizers: ObjectProvider<DownloadConfigBuilderCustomizer>
        ): RuntimeConfig? {
            val logger = LoggerFactory.getLogger(javaClass.getPackage().name + ".EmbeddedMongo")
            val processOutput = ProcessOutput.builder()
                .output(Processors.logTo(logger, Slf4jLevel.INFO))
                .error(Processors.logTo(logger, Slf4jLevel.ERROR))
                .commands(Processors.named("[console>]", Processors.logTo(logger, Slf4jLevel.DEBUG)))
                .build()
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
            val downloadConfig = downloadConfigBuilder.build()
            return Defaults.extractedArtifactStoreFor(Command.MongoD).withDownloadConfig(downloadConfig)
        }
    }

    @ConditionalOnClass(MongoClient::class, ReactiveMongoClientFactoryBean::class)
    internal class ReactiveStreamsDependsOnBeanFactoryPostProcessor :
        ReactiveStreamsMongoClientDependsOnBeanFactoryPostProcessor(MongodExecutable::class.java)
}
