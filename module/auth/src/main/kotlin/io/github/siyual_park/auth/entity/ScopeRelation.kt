package io.github.siyual_park.auth.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.copyDefaultColumn
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("scope_relations")
data class ScopeRelation(
    @Column("parent_id")
    var parentId: Long,
    @Column("child_id")
    var childId: Long,
) : TimeableEntity<ScopeRelation, Long>() {
    override fun clone(): ScopeRelation {
        return copyDefaultColumn(this.copy())
    }
}
