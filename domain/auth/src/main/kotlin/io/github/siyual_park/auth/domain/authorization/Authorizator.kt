package io.github.siyual_park.auth.domain.authorization

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.SuspendSecurityContextHolder
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.auth.exception.PrincipalIdNotExistsException
import io.github.siyual_park.auth.exception.RequiredPermissionException
import org.springframework.stereotype.Component

@Suppress("UNCHECKED_CAST")
@Component
class Authorizator(
    private val scopeTokenStorage: ScopeTokenStorage
) {
    private val strategies = mutableListOf<Pair<AuthorizeFilter, AuthorizeStrategy>>()

    fun register(filter: AuthorizeFilter, strategy: AuthorizeStrategy): Authorizator {
        strategies.add(filter to strategy)
        return this
    }

    suspend fun authorize(
        principal: Principal,
        scope: List<*>,
        targetDomainObjects: List<*>? = null
    ): Boolean {
        return authorizeWithOr(principal, scope, targetDomainObjects)
    }

    private suspend fun authorizeWithOr(
        principal: Principal,
        scope: List<*>,
        targetDomainObjects: List<*>? = null
    ): Boolean {
        if (targetDomainObjects != null && scope.size != targetDomainObjects.size) {
            return false
        }

        for (i in scope.indices) {
            val scopeToken = scope[i]
            if (scopeToken is ScopeToken) {
                if (authorize(principal, scopeToken, targetDomainObjects?.get(i))) {
                    return true
                }
            } else if (scopeToken is String) {
                if (authorize(principal, scopeTokenStorage.loadOrFail(scopeToken), targetDomainObjects?.get(i))) {
                    return true
                }
            } else if (scopeToken is List<*>) {
                if (authorizeWithAnd(principal, scopeToken, targetDomainObjects?.get(i) as? List<Any?>?)) {
                    return true
                }
            } else {
                return false
            }
        }

        return false
    }

    private suspend fun authorizeWithAnd(
        principal: Principal,
        scope: List<*>,
        targetDomainObjects: List<*>? = null
    ): Boolean {
        if (targetDomainObjects != null && scope.size != targetDomainObjects.size) {
            return false
        }

        for (i in scope.indices) {
            val scopeToken = scope[i]
            if (scopeToken is ScopeToken) {
                if (!authorize(principal, scopeToken, targetDomainObjects?.get(i))) {
                    return false
                }
            } else if (scopeToken is String) {
                if (authorize(principal, scopeTokenStorage.loadOrFail(scopeToken), targetDomainObjects?.get(i))) {
                    return true
                }
            } else if (scopeToken is List<*>) {
                if (!authorizeWithOr(principal, scopeToken, targetDomainObjects?.get(i) as? List<Any?>?)) {
                    return false
                }
            } else {
                return false
            }
        }

        return true
    }

    suspend fun authorize(
        principal: Principal,
        scopeToken: String,
        targetDomainObject: Any? = null
    ): Boolean {
        return authorize(principal, scopeTokenStorage.loadOrFail(scopeToken), targetDomainObject)
    }

    suspend fun authorize(
        principal: Principal,
        scopeToken: ScopeToken,
        targetDomainObject: Any? = null
    ): Boolean {
        return strategies.filter { (filter, _) -> filter.isSubscribe(principal, scopeToken) }
            .map { (_, evaluator) -> evaluator }
            .all { it.authorize(principal, scopeToken, targetDomainObject) }
    }
}

suspend inline fun <T> Authorizator.withAuthorize(
    scope: List<*>,
    targetDomainObjects: List<*>? = null,
    func: () -> T
): T {
    val principal = SuspendSecurityContextHolder.getPrincipal() ?: throw PrincipalIdNotExistsException()
    if (!authorize(principal, scope, targetDomainObjects)) {
        throw RequiredPermissionException()
    }

    return func()
}
