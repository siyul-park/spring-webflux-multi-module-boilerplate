package io.github.siyual_park.data.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "application.migration")
data class MigrationProperties(
    val clear: Boolean
)
