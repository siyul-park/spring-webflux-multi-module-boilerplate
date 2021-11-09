package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.auth.entity.ScopeToken

object AllScopeTokenEvaluateFilter : ScopeTokenEvaluateFilter {
    override fun isSubscribe(scopeToken: ScopeToken): Boolean {
        return true
    }
}
