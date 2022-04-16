package io.github.siyual_park.user.entity

import io.github.siyual_park.data.ModifiableLongIDEntity
import io.github.siyual_park.data.annotation.Key
import io.github.siyual_park.ulid.ULID
import org.springframework.data.relational.core.mapping.Table

@Table("user_scopes")
data class UserScopeData(
    @Key("business_keys")
    val userId: ULID,
    @Key("business_keys")
    val scopeTokenId: ULID,
) : ModifiableLongIDEntity()
