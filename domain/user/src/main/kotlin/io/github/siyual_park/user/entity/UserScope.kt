package io.github.siyual_park.user.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.annotation.Key
import io.github.siyual_park.data.copyDefaultColumn
import org.springframework.data.relational.core.mapping.Table

@Table("user_scopes")
data class UserScope(
    @Key("business_keys")
    var userId: Long,
    @Key("business_keys")
    var scopeTokenId: Long,
) : TimeableEntity<UserScope, Long>() {
    override fun clone(): UserScope {
        return copyDefaultColumn(this.copy())
    }
}
