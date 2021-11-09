package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.hasScope
import io.github.siyual_park.auth.entity.ScopeToken

object PrincipalHasScopeTokenEvaluator : ScopeTokenEvaluator {
    override suspend fun evaluate(principal: Principal, targetDomainObject: Any?, scopeToken: ScopeToken): Boolean {
        return principal.hasScope(scopeToken)
    }
}
