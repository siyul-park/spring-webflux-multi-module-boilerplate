package io.github.siyual_park.user.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.copyDefaultColumn
import org.springframework.data.relational.core.mapping.Table

@Table("user_scopes")
data class UserScope(
    var userId: Long,
    var scopeTokenId: Long,
) : TimeableEntity<UserScope, Long>() {
    override fun clone(): UserScope {
        return copyDefaultColumn(this.copy())
    }
}
