package io.github.siyual_park.auth.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.annotation.Key
import org.springframework.data.relational.core.mapping.Table

@Table("scope_relations")
data class ScopeRelationData(
    @Key("business_keys")
    val parentId: Long,
    @Key("business_keys")
    val childId: Long,
) : TimeableEntity<ScopeRelationData, Long>()
