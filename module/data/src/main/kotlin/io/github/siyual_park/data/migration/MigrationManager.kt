package io.github.siyual_park.data.migration

import io.github.siyual_park.data.expansion.columnName
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Sort
import org.springframework.data.mapping.callback.ReactiveEntityCallbacks
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Component

@Component
class MigrationManager(
    connectionFactory: ConnectionFactory,
    entityCallbacks: ReactiveEntityCallbacks? = null
) {
    private val entityTemplate = R2dbcEntityTemplate(connectionFactory)
    private val migrationCheckpointRepository = R2DBCRepository<MigrationCheckpoint, Long>(
        connectionFactory,
        MigrationCheckpoint::class,
        entityCallbacks
    )

    private val migrations = mutableListOf<Migration>()
    private val createMigrationCheckpoint = CreateMigrationCheckpoint()

    init {
        if (entityCallbacks != null) {
            entityTemplate.setEntityCallbacks(entityCallbacks)
        }
    }

    fun register(migration: Migration): MigrationManager {
        migrations.add(migration)
        return this
    }

    suspend fun run() {
        if (!createMigrationCheckpoint.isApplied(entityTemplate)) {
            createMigrationCheckpoint.up(entityTemplate)
        }

        val migrationCheckpoints = migrationCheckpointRepository.findAll(
            sort = Sort.by(columnName(MigrationCheckpoint::version)).descending(),
            limit = 1
        )
            .toList()

        val lastMigrationCheckpoint = migrationCheckpoints.firstOrNull()
        val lastVersion = lastMigrationCheckpoint?.version ?: -1

        for (i in (lastVersion + 1) until migrations.size) {
            val migration = migrations[i]

            migration.up(entityTemplate)

            MigrationCheckpoint(version = i)
                .let { migrationCheckpointRepository.create(it) }
        }
    }

    suspend fun revert() {
        val migrationCheckpoints = migrationCheckpointRepository.findAll()
            .toList()
            .sortedBy { it.version }
            .reversed()

        migrationCheckpoints.forEach {
            val migration = migrations[it.version]
            migration.down(entityTemplate)
            migrationCheckpointRepository.delete(it)
        }

        if (createMigrationCheckpoint.isApplied(entityTemplate)) {
            createMigrationCheckpoint.down(entityTemplate)
        }
    }
}
