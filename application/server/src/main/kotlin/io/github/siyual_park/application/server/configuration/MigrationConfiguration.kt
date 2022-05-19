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
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.r2dbc.core.R2dbcEntityOperations

@Configuration
class MigrationConfiguration(
    private val entityOperations: R2dbcEntityOperations,
    private val mongoTemplate: ReactiveMongoTemplate
) {
    @Autowired(required = true)
    fun configMigrationManager(migrationManager: MigrationManager) {
        migrationManager
            .register(CreateScopeToken(entityOperations))
            .register(CreateScopeRelation(entityOperations))
            .register(CreateToken(mongoTemplate))
            .register(CreateClient(entityOperations, mongoTemplate))
            .register(CreateClientCredential(entityOperations))
            .register(CreateClientScope(entityOperations))
            .register(CreateUser(entityOperations, mongoTemplate))
            .register(CreateUserCredential(entityOperations))
            .register(CreateUserScope(entityOperations))
    }
}
