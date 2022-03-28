package io.github.siyual_park.application.server.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "application.token")
data class TokensProperty(
    val accessToken: TokenProperty,
    val refreshToken: TokenProperty
)
