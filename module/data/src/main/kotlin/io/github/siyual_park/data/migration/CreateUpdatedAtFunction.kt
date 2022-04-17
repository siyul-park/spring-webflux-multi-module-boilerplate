package io.github.siyual_park.data.migration

import org.springframework.data.r2dbc.core.R2dbcEntityOperations

class CreateUpdatedAtFunction(
    private val entityOperations: R2dbcEntityOperations
) : Migration {
    override suspend fun up() {
        if (entityOperations.isDriver("PostgreSQL")) {
            entityOperations.fetchSQL(
                "CREATE OR REPLACE FUNCTION update_timestamp()" +
                    "RETURNS TRIGGER AS $$" +
                    "BEGIN" +
                    "   NEW.updated_at = now();" +
                    "   RETURN NEW;" +
                    "END; $$" +
                    "LANGUAGE plpgsql"
            )
        }
    }

    override suspend fun down() {
        if (entityOperations.isDriver("PostgreSQL")) {
            entityOperations.fetchSQL(
                "DROP FUNCTION update_timestamp;"
            )
        }
    }
}

suspend fun R2dbcEntityOperations.createUpdatedAtTrigger(tableName: String) {
    if (this.isDriver("PostgreSQL")) {
        this.fetchSQL(
            "CREATE TRIGGER ${tableName}_update_timestamp BEFORE UPDATE" +
                "    ON $tableName FOR EACH ROW EXECUTE PROCEDURE" +
                "    update_timestamp();"
        )
    }
}
