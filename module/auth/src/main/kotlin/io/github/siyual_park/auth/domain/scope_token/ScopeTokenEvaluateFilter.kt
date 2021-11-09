package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.auth.entity.ScopeToken

interface ScopeTokenEvaluateFilter {
    fun isSubscribe(scopeToken: ScopeToken): Boolean
}
