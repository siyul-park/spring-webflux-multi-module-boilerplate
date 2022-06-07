package io.github.siyual_park.data.configuration

import io.github.siyual_park.data.property.EmbeddedRedisProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import redis.embedded.RedisServer

@Configuration
class EmbeddedRedisAutoConfiguration(
    private val redisProperties: RedisProperties,
    private val embeddedRedisProperties: EmbeddedRedisProperties
) {
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    fun embeddedRedisServer(): RedisServer? {
        if (!embeddedRedisProperties.enable) {
            return null
        }

        return RedisServer(redisProperties.port)
    }
}
