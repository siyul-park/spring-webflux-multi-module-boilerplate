package io.github.siyual_park.data.migration

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.copyDefaultColumn
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("migration_checkpoints")
data class MigrationCheckpoint(
    @Column("version") var version: Int,
) : TimeableEntity<MigrationCheckpoint, Long>() {
    override fun clone(): MigrationCheckpoint {
        return copyDefaultColumn(this.copy())
    }
}
