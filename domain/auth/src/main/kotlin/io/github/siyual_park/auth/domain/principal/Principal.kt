package io.github.siyual_park.auth.domain.principal

import io.github.siyual_park.auth.entity.ScopeToken
import java.security.Principal as SpringPrincipal

interface Principal : SpringPrincipal {
    val id: String
    val scope: Set<ScopeToken>

    override fun getName(): String {
        return id
    }
}

fun Principal.hasScope(scope: Collection<ScopeToken>): Boolean {
    return this.scope.containsAll(scope)
}
fun Principal.hasScope(scopeToken: ScopeToken): Boolean {
    return this.scope.contains(scopeToken)
}
