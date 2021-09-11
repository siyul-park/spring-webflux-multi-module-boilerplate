package io.github.siyual_park.auth.domain

import io.github.siyual_park.auth.entity.ScopeToken

interface Authentication<ID> : Principal<ID> {
    val scope: Set<ScopeToken>
}

fun <ID> Authentication<ID>.hasScope(scope: Collection<ScopeToken>): Boolean {
    return this.scope.containsAll(scope)
}
fun <ID> Authentication<ID>.hasScope(scopeToken: ScopeToken): Boolean {
    return this.scope.contains(scopeToken)
}
