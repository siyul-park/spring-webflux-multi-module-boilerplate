package io.github.siyual_park.auth.migration

import io.github.siyual_park.data.migration.Migration
import io.github.siyual_park.data.migration.createIndex
import io.github.siyual_park.data.migration.dropTable
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate

class CreateUserScope : Migration {
    private val tableName = "user_scopes"

    override suspend fun up(entityTemplate: R2dbcEntityTemplate) {
        entityTemplate.databaseClient.sql(
            "CREATE TABLE $tableName" +
                "(" +
                "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +

                "user_id BIGINT NOT NULL REFERENCES users (id)," +
                "scope_token_id BIGINT NOT NULL REFERENCES scope_tokens (id)," +

                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")"
        )
            .fetch()
            .rowsUpdated()
            .awaitSingle()

        entityTemplate.createIndex(tableName, listOf("user_id"))
        entityTemplate.createIndex(tableName, listOf("scope_token_id"))
    }
    override suspend fun down(entityTemplate: R2dbcEntityTemplate) {
        entityTemplate.dropTable(tableName)
    }
}
