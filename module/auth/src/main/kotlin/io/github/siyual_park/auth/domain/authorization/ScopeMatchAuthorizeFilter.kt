package io.github.siyual_park.auth.domain.authorization

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.entity.ScopeToken

class ScopeMatchAuthorizeFilter(
    private val scopeTokens: Set<String>
) : AuthorizeFilter {
    override fun isSubscribe(principal: Principal, scopeToken: ScopeToken): Boolean {
        return scopeTokens.contains(scopeToken.name)
    }
}
