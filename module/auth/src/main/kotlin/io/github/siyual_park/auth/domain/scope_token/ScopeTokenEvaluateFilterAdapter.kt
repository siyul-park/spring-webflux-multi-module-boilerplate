package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.auth.annotation.ScopeTokenFilter
import io.github.siyual_park.auth.entity.ScopeToken

class ScopeTokenEvaluateFilterAdapter(
    private val value: ScopeTokenFilter
) : ScopeTokenEvaluateFilter {
    override fun isSubscribe(scopeToken: ScopeToken): Boolean {
        return value.names.any { it == scopeToken.name }
    }
}
