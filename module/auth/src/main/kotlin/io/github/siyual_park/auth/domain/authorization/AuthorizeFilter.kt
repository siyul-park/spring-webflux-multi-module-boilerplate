package io.github.siyual_park.auth.domain.authorization

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.entity.ScopeToken

interface AuthorizeFilter {
    fun isSubscribe(principal: Principal, scopeToken: ScopeToken): Boolean
}
