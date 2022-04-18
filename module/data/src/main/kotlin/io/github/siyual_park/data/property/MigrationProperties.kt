package io.github.siyual_park.data.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "application.migration")
data class MigrationProperties(
    val sync: Boolean,
    val clear: Boolean
)
