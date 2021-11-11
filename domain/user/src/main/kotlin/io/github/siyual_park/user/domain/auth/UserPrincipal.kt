package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.client.entity.ClientEntity
import io.github.siyual_park.user.entity.UserEntity

data class UserPrincipal(
    override val id: String,
    override val clientId: Long? = null,
    override val scope: Set<ScopeToken>,
) : Principal, UserEntity, ClientEntity {
    override val userId: Long
        get() = id.toLong()
}
