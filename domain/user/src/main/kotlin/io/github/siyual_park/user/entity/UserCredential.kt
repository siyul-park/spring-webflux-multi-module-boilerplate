package io.github.siyual_park.user.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.annotation.Key
import org.springframework.data.relational.core.mapping.Table

@Table("user_credentials")
data class UserCredential(
    @Key
    val userId: Long,
    var password: String,
    var hashAlgorithm: String,
) : TimeableEntity<UserCredential, Long>()
