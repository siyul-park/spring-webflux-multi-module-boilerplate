package io.github.siyual_park.application.server.gateway

import io.github.siyual_park.application.server.helper.AuthorizationHeaderGenerator
import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Suppress("NAME_SHADOWING")
@Component
class GatewayAuthorization(
    private val authorizationHeaderGenerator: AuthorizationHeaderGenerator,
    private val scopeTokenStorage: ScopeTokenStorage,
) {
    private var principal: Principal? = null

    suspend fun setPrincipal(
        principal: Principal,
        push: List<String> = emptyList(),
        pop: List<String> = emptyList()
    ) {
        val push = scopeTokenStorage.load(push).toList()
        val pop = scopeTokenStorage.load(pop).toList()

        val scope = mutableSetOf<ScopeToken>()

        scope.addAll(
            principal.scope.flatMap { it.resolve().toList() }
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
