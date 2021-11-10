package io.github.siyual_park.auth.domain.authorization

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.entity.ScopeToken

abstract class ScopeMatchAuthorizeProcessor<PRINCIPAL : Principal>(
    scope: String
) : AuthorizeProcessor<PRINCIPAL> {
    private val scopeTokens = scope.split(" ").toSet()

    override suspend fun authorize(principal: PRINCIPAL, scopeToken: ScopeToken, targetDomainObject: Any?): Boolean {
        if (!scopeTokens.contains(scopeToken.name)) {
            return true
        }
        return authorize(principal, targetDomainObject)
    }

    abstract suspend fun authorize(principal: PRINCIPAL, targetDomainObject: Any? = null): Boolean
}
