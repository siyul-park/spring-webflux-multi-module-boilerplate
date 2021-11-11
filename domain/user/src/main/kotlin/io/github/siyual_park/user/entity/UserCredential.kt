package io.github.siyual_park.user.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.annotation.Key
import io.github.siyual_park.data.copyDefaultColumn
import org.springframework.data.relational.core.mapping.Table

@Table("user_credentials")
data class UserCredential(
    @Key
    var userId: Long,
    var password: String,
    var hashAlgorithm: String,
) : TimeableEntity<UserCredential, Long>() {
    override fun clone(): UserCredential {
        return copyDefaultColumn(this.copy())
    }
}
