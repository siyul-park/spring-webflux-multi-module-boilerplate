package io.github.siyual_park.auth.domain.authorization

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.hasScope
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import org.springframework.stereotype.Component

@Component
@AuthorizeMapping(AllowAllAuthorizeFilter::class)
class PrincipalHasScopeAuthorizeStrategy : AuthorizeStrategy {
    override suspend fun authorize(principal: Principal, scopeToken: ScopeToken, targetDomainObject: Any?): Boolean {
        return principal.hasScope(scopeToken)
    }
}
