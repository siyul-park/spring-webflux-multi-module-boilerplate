package io.github.siyual_park.auth.spring

import io.github.siyual_park.auth.entity.ScopeToken
import org.springframework.security.core.GrantedAuthority

class GrantedAuthorityAdapter(
    scopeToken: ScopeToken
) : GrantedAuthority {
    private val scopeTokenId = scopeToken.id.toString()

    override fun getAuthority(): String {
        return scopeTokenId
    }
}
