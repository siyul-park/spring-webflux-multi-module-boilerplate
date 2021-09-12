package io.github.siyual_park.user.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.copyDefaultColumn
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("user_scopes")
data class UserScope(
    @Column("user_id")
    var userId: Long,
    @Column("scope_token_id")
    var scopeTokenId: Long,
) : TimeableEntity<UserScope, Long>() {
    override fun clone(): UserScope {
        return copyDefaultColumn(this.copy())
    }
}
