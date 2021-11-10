package io.github.siyual_park.auth.domain.authorization

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.entity.ScopeToken
import org.springframework.stereotype.Component

@Component
class AllAuthorizeFilter : AuthorizeFilter {
    override fun isSubscribe(principal: Principal, scopeToken: ScopeToken): Boolean {
        return true
    }
}
