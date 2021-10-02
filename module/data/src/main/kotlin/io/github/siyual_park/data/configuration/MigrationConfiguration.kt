package io.github.siyual_park.data.configuration

import io.github.siyual_park.data.migration.MigrationManager
import kotlinx.coroutines.runBlocking
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order

@Configuration
class MigrationConfiguration(
    private val migrationManager: MigrationManager,
    private val migrationConfigurationProperty: MigrationConfigurationProperty
) {
    @EventListener(ApplicationReadyEvent::class)
    @Order(0)
    fun migration() = runBlocking {
        if (migrationConfigurationProperty.sync) {
            migrationManager.sync()
        }
    }
}
