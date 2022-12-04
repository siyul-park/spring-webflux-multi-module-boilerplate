package io.github.siyual_park.client.property

import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URL

@ConfigurationProperties(prefix = "application.client.root")
data class RootClientProperty(
    val id: String,
    val name: String,
    val secret: String,
    val origin: URL
)
