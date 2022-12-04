package io.github.siyual_park.data.configuration

import com.mongodb.MongoClientSettings
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongodConfig
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.MutablePropertySources

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MongoProperties::class)
@AutoConfigureBefore(MongoAutoConfiguration::class, MongoReactiveAutoConfiguration::class)
@ConditionalOnClass(MongoClientSettings::class, MongodStarter::class)
@ConditionalOnProperty(name = ["spring.mongodb.embedded.enable"], havingValue = "true")
class EmbeddedMongoAutoCustomConfiguration(
    private val mongoProperties: MongoProperties,
) {

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    fun embeddedMongoServer(
        mongodConfig: MongodConfig,
        context: ApplicationContext
    ): MongodExecutable? {
        val configuredPort = mongoProperties.port
        if (configuredPort == null || configuredPort == 0) {
            setEmbeddedPort(context, mongodConfig.net().port)
        }
        val mongodStarter = MongodStarter.getDefaultInstance()
        return mongodStarter.prepare(mongodConfig)
    }

    private fun setEmbeddedPort(context: ApplicationContext, port: Int) {
        setPortProperty(context, port)
    }

    private fun setPortProperty(currentContext: ApplicationContext, port: Int) {
        if (currentContext is ConfigurableApplicationContext) {
            getMongoPorts(currentContext.environment.propertySources)?.put("local.mongo.port", port)
        }
        currentContext.parent?.let { setPortProperty(it, port) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getMongoPorts(sources: MutablePropertySources): MutableMap<String?, Any?>? {
        var propertySource = sources["mongo.ports"]
        if (propertySource == null) {
            propertySource = MapPropertySource("mongo.ports", HashMap())
            sources.addFirst(propertySource)
        }
        return propertySource.source as? MutableMap<String?, Any?>
    }
}
