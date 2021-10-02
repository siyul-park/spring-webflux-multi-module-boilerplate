package io.github.siyual_park.data.migration

import io.github.siyual_park.data.expansion.columnName
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Component

@Component
class MigrationManager(
    private val entityTemplate: R2dbcEntityTemplate,
) {
    private val migrationCheckpointRepository = R2DBCRepository<MigrationCheckpoint, Long>(
        entityTemplate,
        MigrationCheckpoint::class,
    )

    private val migrations = mutableListOf<Migration>()
    private val createMigrationCheckpoint = CreateMigrationCheckpoint()

    fun register(migration: Migration): MigrationManager {
        migrations.add(migration)
        return this
    }

    suspend fun sync() {
        // TODO(특정 버전 동기화 지원)
        run()
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
        val migrationCheckpoints = migrationCheckpointRepository.findAll(
            sort = Sort.by(columnName(MigrationCheckpoint::version)).descending()
        )
            .toList()

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
