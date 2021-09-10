package io.github.siyual_park.data.migration

import io.github.siyual_park.data.Cloneable
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("migration_checkpoints")
data class MigrationCheckpoint(
    @Column("version") var version: Int,

    @Id var id: Long? = null,
    @Column("created_at") var createdAt: Instant? = null
): Cloneable<MigrationCheckpoint> {
    override fun clone(): MigrationCheckpoint {
        return this.copy()
    }
}