package io.github.siyual_park.client.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "application.client.root")
data class RootClientProperty(
    val name: String,
    val secret: String
)
