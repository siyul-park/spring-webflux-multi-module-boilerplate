package io.github.siyual_park.auth.entity

import io.github.siyual_park.data.AutoModifiable
import io.github.siyual_park.data.Modifiable
import io.github.siyual_park.data.ULIDEntity
import io.github.siyual_park.data.annotation.Key
import io.github.siyual_park.ulid.ULID
import org.springframework.data.relational.core.mapping.Table

@Table("scope_relations")
data class ScopeRelationData(
    @Key("business_keys")
    val parentId: ULID,
    @Key("business_keys")
    val childId: ULID,
) : ULIDEntity(), Modifiable by AutoModifiable()
