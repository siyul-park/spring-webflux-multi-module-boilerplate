package io.github.siyual_park.user.entity

import io.github.siyual_park.data.ModifiableULIDEntity
import io.github.siyual_park.data.annotation.Key
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class UserEntity(
    @Key
    var name: String,
    @Key
    var email: String,

    var hashAlgorithm: String,
    var password: String,
) : ModifiableULIDEntity()
