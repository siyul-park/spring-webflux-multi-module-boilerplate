package io.github.siyual_park.data.migration

import io.github.siyual_park.data.expansion.columnName
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Component

@Component
class MigrationManager(
    private val entityOperations: R2dbcEntityOperations,
) {
    private val logger = LoggerFactory.getLogger(MigrationManager::class.java)

    private val migrationCheckpointRepository = R2DBCRepository<MigrationCheckpoint, Long>(
        entityOperations,
        MigrationCheckpoint::class,
    )

    private val migrations = mutableListOf<Migration>()

    private val createUpdatedAtFunction = CreateUpdatedAtFunction()
    private val createMigrationCheckpoint = CreateMigrationCheckpoint()

    fun register(migration: Migration): MigrationManager {
        migrations.add(migration)
        return this
    }

    suspend fun sync() {
        try {
            run()
        } catch (e: RuntimeException) {
            logger.error(e.message, e)
            throw e
        }
    }

    suspend fun run() {
        if (!createMigrationCheckpoint.isApplied(entityOperations)) {
            createUpdatedAtFunction.up(entityOperations)
            createMigrationCheckpoint.up(entityOperations)
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

            migration.up(entityOperations)

            MigrationCheckpoint(version = i)
                .let { migrationCheckpointRepository.create(it) }
        }
    }

    suspend fun clear() {
        migrations.asReversed()
            .forEach {
                try {
                    it.down(entityOperations)
                } catch (e: RuntimeException) {
                    logger.error(e.message, e)
                }
            }

        try {
            if (createMigrationCheckpoint.isApplied(entityOperations)) {
                createMigrationCheckpoint.down(entityOperations)
                createUpdatedAtFunction.down(entityOperations)
            }
        } catch (e: RuntimeException) {
            logger.error(e.message, e)
        }
    }

    suspend fun revert() {
        val migrationCheckpoints = migrationCheckpointRepository.findAll(
            sort = Sort.by(columnName(MigrationCheckpoint::version)).descending()
        )
            .toList()

        migrationCheckpoints.forEach {
            val migration = migrations[it.version]
            migration.down(entityOperations)
            migrationCheckpointRepository.delete(it)
        }

        if (createMigrationCheckpoint.isApplied(entityOperations)) {
            createMigrationCheckpoint.down(entityOperations)
            createUpdatedAtFunction.down(entityOperations)
        }
    }
}
