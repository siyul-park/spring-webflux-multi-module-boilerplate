package io.github.siyual_park.auth.domain.authorization

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.ScopeToken

interface AuthorizeStrategy {
    suspend fun authorize(principal: Principal, scopeToken: ScopeToken, targetDomainObject: Any? = null): Boolean
}
