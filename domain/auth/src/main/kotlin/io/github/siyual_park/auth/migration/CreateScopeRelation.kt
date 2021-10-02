package io.github.siyual_park.auth.migration

import io.github.siyual_park.data.migration.Migration
import io.github.siyual_park.data.migration.createUniqueIndex
import io.github.siyual_park.data.migration.dropTable
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate

class CreateScopeRelation : Migration {
    private val tableName = "scope_relations"

    override suspend fun up(entityTemplate: R2dbcEntityTemplate) {
        entityTemplate.databaseClient.sql(
            "CREATE TABLE $tableName" +
                "(" +
                "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +

                "parent_id BIGINT NOT NULL REFERENCES scope_tokens (id)," +
                "child_id BIGINT NOT NULL REFERENCES scope_tokens (id)," +

                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")"
        )
            .fetch()
            .rowsUpdated()
            .awaitSingle()

        entityTemplate.createUniqueIndex(tableName, listOf("parent_id", "child_id"))
    }

    override suspend fun down(entityTemplate: R2dbcEntityTemplate) {
        entityTemplate.dropTable(tableName)
    }
}
