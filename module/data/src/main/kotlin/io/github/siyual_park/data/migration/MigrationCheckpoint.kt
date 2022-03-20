package io.github.siyual_park.data.migration

import io.github.siyual_park.data.TimeableEntity
import org.springframework.data.relational.core.mapping.Table

@Table("migration_checkpoints")
data class MigrationCheckpoint(
    var version: Int,
    var status: MigrationStatus
) : TimeableEntity<MigrationCheckpoint, Long>()
