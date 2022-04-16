package io.github.siyual_park.user.entity

import io.github.siyual_park.data.AutoModifiable
import io.github.siyual_park.data.Modifiable
import io.github.siyual_park.data.SoftDeletable
import io.github.siyual_park.data.ULIDEntity
import io.github.siyual_park.data.annotation.Key
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("users")
data class UserData(
    @Key
    var name: String,
    @Key
    var email: String,
    override var deletedAt: Instant? = null
) : ULIDEntity(), Modifiable by AutoModifiable(), SoftDeletable
