package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.entity.ScopeToken
import org.springframework.stereotype.Component

@Component
class ScopeEvaluator : ScopeTokenEvaluator {
    private val evaluators = mutableListOf<Pair<ScopeTokenEvaluateFilter, ScopeTokenEvaluator>>()

    fun register(filter: ScopeTokenEvaluateFilter, evaluator: ScopeTokenEvaluator): ScopeEvaluator {
        evaluators.add(filter to evaluator)
        return this
    }

    suspend fun evaluate(principal: Principal, targetDomainObject: Any?, scope: Collection<ScopeToken>): Boolean {
        return scope.all { evaluate(principal, targetDomainObject, it) }
    }

    override suspend fun evaluate(principal: Principal, targetDomainObject: Any?, scopeToken: ScopeToken): Boolean {
        return evaluators.filter { (filter, _) -> filter.isSubscribe(scopeToken) }
            .map { (_, evaluator) -> evaluator }
            .all { it.evaluate(principal, targetDomainObject, scopeToken) }
    }
}
