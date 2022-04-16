package io.github.siyual_park.auth.entity

import io.github.siyual_park.data.ModifiableLongIDEntity
import io.github.siyual_park.data.annotation.Key
import io.github.siyual_park.ulid.ULID
import org.springframework.data.relational.core.mapping.Table

@Table("scope_relations")
data class ScopeRelationData(
    @Key("business_keys")
    val parentId: ULID,
    @Key("business_keys")
    val childId: ULID,
) : ModifiableLongIDEntity()
