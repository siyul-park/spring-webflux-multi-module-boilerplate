package io.github.siyual_park.auth.domain.authenticator

import io.github.siyual_park.auth.entity.ScopeToken
import java.security.Principal as DefaultPrincipal

interface Principal<ID> : DefaultPrincipal {
    val id: ID
    val scope: Set<ScopeToken>

    override fun getName(): String {
        return id.toString()
    }
}

fun <ID> Principal<ID>.hasScope(scope: Collection<ScopeToken>): Boolean {
    return this.scope.containsAll(scope)
}
fun <ID> Principal<ID>.hasScope(scopeToken: ScopeToken): Boolean {
    return this.scope.contains(scopeToken)
}
