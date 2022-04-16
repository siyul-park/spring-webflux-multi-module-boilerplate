package io.github.siyual_park.user.entity

import io.github.siyual_park.data.ModifiableLongIDEntity
import io.github.siyual_park.data.annotation.Key
import io.github.siyual_park.ulid.ULID
import org.springframework.data.relational.core.mapping.Table

@Table("user_credentials")
data class UserCredentialData(
    @Key
    val userId: ULID,
    var password: String,
    var hashAlgorithm: String,
) : ModifiableLongIDEntity()
