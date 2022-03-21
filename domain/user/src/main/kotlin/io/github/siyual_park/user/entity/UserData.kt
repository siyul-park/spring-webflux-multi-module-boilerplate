package io.github.siyual_park.user.entity

import io.github.siyual_park.data.SoftDeletable
import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.annotation.Key
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("users")
data class UserData(
    @Key
    var name: String,
    override var deletedAt: Instant? = null
) : TimeableEntity<UserData, Long>(), SoftDeletable
