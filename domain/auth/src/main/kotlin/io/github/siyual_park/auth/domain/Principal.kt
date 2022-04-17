package io.github.siyual_park.auth.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeToken

interface Principal {
    val id: String
    var scope: Set<ScopeToken>
}

fun Principal.hasScope(scope: Collection<ScopeToken>): Boolean {
    return this.scope.containsAll(scope)
}
fun Principal.hasScope(scopeToken: ScopeToken): Boolean {
    return this.scope.contains(scopeToken)
}
