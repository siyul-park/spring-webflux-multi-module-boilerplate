package io.github.siyual_park.auth.domain.authorization

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.entity.ScopeToken

class SimpleAuthorizeFilter<T>(
    private val principal: Class<T>,
    scope: String
) : AuthorizeFilter {
    private val scopeTokens = scope.split(" ").toSet()

    override fun isSubscribe(principal: Principal, scopeToken: ScopeToken): Boolean {
        return this.principal.isInstance(principal) && scopeTokens.contains(scopeToken.name)
    }
}
