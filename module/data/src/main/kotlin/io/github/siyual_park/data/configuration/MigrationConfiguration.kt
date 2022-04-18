package io.github.siyual_park.data.configuration

import io.github.siyual_park.data.migration.MigrationManager
import io.github.siyual_park.data.property.MigrationProperties
import kotlinx.coroutines.runBlocking
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order

@Configuration
class MigrationConfiguration(
    private val migrationManager: MigrationManager,
    private val property: MigrationProperties
) {
    @EventListener(ApplicationReadyEvent::class)
    @Order(0)
    fun migration() = runBlocking {
        if (property.clear) {
            migrationManager.clear()
        }
        if (property.sync) {
            migrationManager.sync()
        }
    }
}
