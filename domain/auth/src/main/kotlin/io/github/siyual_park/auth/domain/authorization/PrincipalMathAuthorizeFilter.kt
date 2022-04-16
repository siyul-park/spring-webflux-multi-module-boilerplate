package io.github.siyual_park.auth.domain.authorization

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import kotlin.reflect.KClass

class PrincipalMathAuthorizeFilter(
    private val clazz: KClass<*>
) : AuthorizeFilter {
    override fun isSubscribe(principal: Principal, scopeToken: ScopeToken): Boolean {
        return clazz.isInstance(principal)
    }
}
