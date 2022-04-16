package io.github.siyual_park.user.entity

import io.github.siyual_park.data.AutoModifiable
import io.github.siyual_park.data.Modifiable
import io.github.siyual_park.data.ULIDEntity
import io.github.siyual_park.data.annotation.Key
import io.github.siyual_park.ulid.ULID
import org.springframework.data.relational.core.mapping.Table

@Table("user_credentials")
data class UserCredentialData(
    @Key
    val userId: ULID,
    var password: String,
    var hashAlgorithm: String,
) : ULIDEntity(), Modifiable by AutoModifiable()
