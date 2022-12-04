package io.github.siyual_park.application.server.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "application.token")
data class TokensProperty(
    val accessToken: TokenProperty,
    val refreshToken: TokenProperty
)
