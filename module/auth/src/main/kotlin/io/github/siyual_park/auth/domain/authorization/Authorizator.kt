package io.github.siyual_park.auth.domain.authorization

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.entity.ScopeToken
import org.springframework.stereotype.Component

@Suppress("UNCHECKED_CAST")
@Component
class Authorizator {
    private val authorizators = mutableListOf<Pair<AuthorizeFilter, AuthorizeProcessor<*>>>()

    fun register(filter: AuthorizeFilter, processor: AuthorizeProcessor<*>): Authorizator {
        authorizators.add(filter to processor)
        return this
    }

    suspend fun <PRINCIPAL : Principal> authorize(
        principal: PRINCIPAL,
        scope: Collection<ScopeToken>,
        targetDomainObject: Any? = null
    ): Boolean {
        return scope.all { authorize(principal, it, targetDomainObject) }
    }

    suspend fun <PRINCIPAL : Principal> authorize(
        principal: PRINCIPAL,
        scopeToken: ScopeToken,
        targetDomainObject: Any? = null
    ): Boolean {
        return authorizators.filter { (filter, _) -> filter.isSubscribe(principal, scopeToken) }
            .map { (_, evaluator) -> evaluator as? AuthorizeProcessor<PRINCIPAL> }
            .filterNotNull()
            .all { it.authorize(principal, scopeToken, targetDomainObject) }
    }
}
