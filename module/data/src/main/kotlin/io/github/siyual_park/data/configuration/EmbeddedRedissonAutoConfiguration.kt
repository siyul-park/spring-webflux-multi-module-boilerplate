package io.github.siyual_park.data.configuration

import de.flapdoodle.embed.process.runtime.Network
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.spring.starter.RedissonAutoConfiguration
import org.redisson.spring.starter.RedissonProperties
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.data.redis.core.RedisOperations
import redis.embedded.RedisServer

@Configuration
@ConditionalOnClass(RedisServer::class, Redisson::class, RedisOperations::class)
@EnableConfigurationProperties(RedissonProperties::class, RedisProperties::class)
@AutoConfigureBefore(RedissonAutoConfiguration::class)
@ConditionalOnProperty(name = ["spring.redis.embedded.enable"], havingValue = "true")
class EmbeddedRedissonAutoConfiguration(
    private val redisProperties: RedisProperties,
) : RedissonAutoConfiguration() {
    init {
        redisProperties.port = Network.getFreeServerPort()
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean(RedisServer::class)
    fun redisServer(): RedisServer {
        return RedisServer(redisProperties.port)
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient::class)
    @DependsOn("redisServer")
    override fun redisson(): RedissonClient {
        return super.redisson()
    }
}
