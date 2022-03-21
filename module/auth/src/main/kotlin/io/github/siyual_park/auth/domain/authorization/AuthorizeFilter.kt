package io.github.siyual_park.auth.domain.authorization

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.ScopeToken

interface AuthorizeFilter {
    fun isSubscribe(principal: Principal, scopeToken: ScopeToken): Boolean
}

fun AuthorizeFilter.then(filter: AuthorizeFilter): AuthorizeFilter {
    val self = this
    return object : AuthorizeFilter {
        override fun isSubscribe(principal: Principal, scopeToken: ScopeToken): Boolean {
            return self.isSubscribe(principal, scopeToken) && filter.isSubscribe(principal, scopeToken)
        }
    }
}
