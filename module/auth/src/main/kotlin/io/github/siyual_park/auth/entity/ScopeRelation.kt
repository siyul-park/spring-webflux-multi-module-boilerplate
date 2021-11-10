package io.github.siyual_park.auth.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.annotation.Key
import io.github.siyual_park.data.copyDefaultColumn
import org.springframework.data.relational.core.mapping.Table

@Table("scope_relations")
data class ScopeRelation(
    @Key("business_keys")
    var parentId: Long,
    @Key("business_keys")
    var childId: Long,
) : TimeableEntity<ScopeRelation, Long>() {
    override fun clone(): ScopeRelation {
        return copyDefaultColumn(this.copy())
    }
}
