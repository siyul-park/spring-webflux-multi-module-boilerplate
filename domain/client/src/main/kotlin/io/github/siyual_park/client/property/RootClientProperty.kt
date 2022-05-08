package io.github.siyual_park.client.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.net.URL

@ConstructorBinding
@ConfigurationProperties(prefix = "application.client.root")
data class RootClientProperty(
    val id: String,
    val name: String,
    val secret: String,
    val origin: URL
)
