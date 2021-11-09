package io.github.siyual_park.data.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "migration")
data class MigrationConfigurationProperty(
    val sync: Boolean,
    val clear: Boolean
)
