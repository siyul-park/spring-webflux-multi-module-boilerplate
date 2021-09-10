package io.github.siyual_park.data.configuration

import io.github.siyual_park.data.migration.MigrationManager
import kotlinx.coroutines.runBlocking
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener

@Configuration
class DataConfiguration(
    private val migrationManager: MigrationManager
) {
    @EventListener(ApplicationReadyEvent::class)
    fun migration() {
        runBlocking {
            migrationManager.run()
        }
    }
}
