package io.github.siyual_park.auth.domain.authorization

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.entity.ScopeToken

interface AuthorizeProcessor<PRINCIPAL : Principal> {
    suspend fun authorize(principal: PRINCIPAL, scopeToken: ScopeToken, targetDomainObject: Any? = null): Boolean
}
