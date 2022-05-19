package io.github.siyual_park.user.entity

import io.github.siyual_park.data.ModifiableULIDEntity
import io.github.siyual_park.data.annotation.Key
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class UserData(
    @Key
    var name: String,
    @Key
    var email: String,
) : ModifiableULIDEntity()
