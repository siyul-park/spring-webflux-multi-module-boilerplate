package io.github.siyual_park.application.server.configuration

import io.github.siyual_park.auth.migration.CreateScopeRelation
import io.github.siyual_park.auth.migration.CreateScopeToken
import io.github.siyual_park.auth.migration.CreateToken
import io.github.siyual_park.client.migration.CreateClient
import io.github.siyual_park.client.migration.CreateClientCredential
import io.github.siyual_park.client.migration.CreateClientScope
import io.github.siyual_park.data.migration.MigrationManager
import io.github.siyual_park.user.migration.CreateUser
import io.github.siyual_park.user.migration.CreateUserCredential
import io.github.siyual_park.user.migration.CreateUserScope
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
class MigrationConfiguration {
    @Autowired(required = true)
    fun configMigrationManager(migrationManager: MigrationManager) {
        migrationManager
            .register(CreateScopeToken())
            .register(CreateScopeRelation())
            .register(CreateToken())
            .register(CreateClient())
            .register(CreateClientCredential())
            .register(CreateClientScope())
            .register(CreateUser())
            .register(CreateUserCredential())
            .register(CreateUserScope())
    }
}
