package io.github.siyual_park.auth.domain.authorization

import io.github.siyual_park.auth.domain.scope_token.ScopeToken

interface Authorizable {
    suspend fun has(scopeToken: ScopeToken): Boolean
    suspend fun grant(scopeToken: ScopeToken)
    suspend fun revoke(scopeToken: ScopeToken)
}
