package io.github.siyual_park.data.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "spring.redis.embedded")
data class EmbeddedRedisProperties(
    val enable: Boolean
)
