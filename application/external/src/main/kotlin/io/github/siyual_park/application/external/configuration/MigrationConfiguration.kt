package io.github.siyual_park.application.external.configuration

import io.github.siyual_park.auth.migration.CreateScopeToken
import io.github.siyual_park.data.migration.MigrationManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
class MigrationConfiguration {
    @Autowired(required = true)
    fun configMigrationManager(migrationManager: MigrationManager) {
        migrationManager
            .register(CreateScopeToken())
    }
}
