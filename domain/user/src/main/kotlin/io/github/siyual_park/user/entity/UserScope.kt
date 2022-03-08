package io.github.siyual_park.user.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.annotation.Key
import org.springframework.data.relational.core.mapping.Table

@Table("user_scopes")
data class UserScope(
    @Key("business_keys")
    val userId: Long,
    @Key("business_keys")
    val scopeTokenId: Long,
) : TimeableEntity<UserScope, Long>()
