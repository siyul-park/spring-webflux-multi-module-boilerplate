package io.github.siyual_park.auth.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.ulid.ULID

interface Principal {
    val id: ULID
    var scope: Set<ScopeToken>
}

fun Principal.hasScope(scope: Collection<ScopeToken>): Boolean {
    return this.scope.containsAll(scope)
}
fun Principal.hasScope(scopeToken: ScopeToken): Boolean {
    return this.scope.contains(scopeToken)
}
