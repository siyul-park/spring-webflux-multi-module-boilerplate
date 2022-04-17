package io.github.siyual_park.data.migration

import io.github.siyual_park.data.expansion.columnName
import io.github.siyual_park.data.repository.r2dbc.SimpleR2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.where
import io.github.siyual_park.data.repository.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class MigrationManager(
    private val entityOperations: R2dbcEntityOperations,
) {
    private val logger = LoggerFactory.getLogger(MigrationManager::class.java)

    private val migrationCheckpointRepository = SimpleR2DBCRepository<MigrationCheckpoint, Long>(
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
        run()
    }

    suspend fun run(): Unit = logging {
        if (!createMigrationCheckpoint.isApplied(entityOperations)) {
            createUpdatedAtFunction.up(entityOperations)
            createMigrationCheckpoint.up(entityOperations)
        }

        while (
            migrationCheckpointRepository.exists(where(MigrationCheckpoint::status).`is`(MigrationStatus.PENDING))
        ) {
            delay(Duration.ofSeconds(30).toMillis())
        }

        migrationCheckpointRepository.deleteAll(
            where(MigrationCheckpoint::status).`is`(MigrationStatus.REJECT)
        )

        val migrationCheckpoints = migrationCheckpointRepository.findAll(
            criteria = where(MigrationCheckpoint::status).`is`(MigrationStatus.COMPLETE),
            sort = Sort.by(columnName(MigrationCheckpoint::version)).descending(),
            limit = 1
        )
            .toList()

        val lastMigrationCheckpoint = migrationCheckpoints.firstOrNull()
        val lastVersion = lastMigrationCheckpoint?.version ?: -1

        for (i in (lastVersion + 1) until migrations.size) {
            val migration = migrations[i]
            val checkpoint = try {
                MigrationCheckpoint(version = i, status = MigrationStatus.PENDING)
                    .let { migrationCheckpointRepository.create(it) }
            } catch (e: DataIntegrityViolationException) {
                waitingComplete()
                return@logging
            }

            try {
                migration.up(entityOperations)
                migrationCheckpointRepository.update(checkpoint) {
                    it.status = MigrationStatus.COMPLETE
                }
            } catch (e: Exception) {
                migrationCheckpointRepository.update(checkpoint) {
                    it.status = MigrationStatus.REJECT
                }
                throw e
            }
        }
    }

    suspend fun clear() = logging {
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

    suspend fun revert() = logging {
        val migrationCheckpoints = migrationCheckpointRepository.findAll(
            criteria = where(MigrationCheckpoint::status).`is`(MigrationStatus.COMPLETE),
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

    private suspend fun waitingComplete() {
        while (
            migrationCheckpointRepository.count(where(MigrationCheckpoint::status).`is`(MigrationStatus.COMPLETE))
            == migrations.size.toLong()
        ) {
            delay(Duration.ofSeconds(30).toMillis())
        }
    }

    private suspend fun <T> logging(func: suspend () -> T): T {
        try {
            return func()
        } catch (e: RuntimeException) {
            logger.error(e.message, e)
            throw e
        }
    }
}
