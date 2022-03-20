package io.github.siyual_park.application.server.gateway

import io.github.siyual_park.application.server.helper.AuthorizationHeaderGenerator
import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFinder
import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.auth.entity.ids
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class GatewayAuthorization(
    private val authorizationHeaderGenerator: AuthorizationHeaderGenerator,
    private val scopeTokenFinder: ScopeTokenFinder,
) {
    private var principal: Principal? = null

    suspend fun setPrincipal(
        principal: Principal,
        push: List<String> = emptyList(),
        pop: List<String> = emptyList()
    ) {
        val push = scopeTokenFinder.findAllByName(push)
        val pop = scopeTokenFinder.findAllByName(pop)

        val scope = mutableSetOf<ScopeToken>()

        scope.addAll(
            scopeTokenFinder.findAllWithResolved(principal.scope.ids())
                .filter { token -> pop.firstOrNull { it.id == token.id } == null }
                .toList()
        )
        scope.addAll(push.toList())

        principal.scope = scope

        this.principal = principal
    }

    suspend fun getAuthorization(): String {
        return principal?.let { authorizationHeaderGenerator.generate(it) } ?: throw RuntimeException()
    }
}
