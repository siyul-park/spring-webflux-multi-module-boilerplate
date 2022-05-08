package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.PrincipalMapping
import io.github.siyual_park.auth.domain.principal_refresher.PrincipalRefreshStrategy
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.client.domain.ClientStorage
import io.github.siyual_park.persistence.AsyncLazy
import io.github.siyual_park.persistence.loadOrFail
import kotlinx.coroutines.flow.toSet
import org.springframework.stereotype.Component

@Component
@PrincipalMapping(ClientPrincipal::class)
class ClientPrincipalRefreshStrategy(
    private val clientStorage: ClientStorage,
    private val scopeTokenStorage: ScopeTokenStorage
) : PrincipalRefreshStrategy<ClientPrincipal> {
    private val accessTokenScope = AsyncLazy {
        scopeTokenStorage.loadOrFail("access-token:create")
    }
    private val refreshTokenScope = AsyncLazy {
        scopeTokenStorage.loadOrFail("refresh-token:create")
    }

    override suspend fun refresh(principal: ClientPrincipal): ClientPrincipal {
        val client = clientStorage.loadOrFail(principal.clientId)
        val clientScope = client.getScope().toSet()

        return ClientPrincipal(
            id = principal.id,
            scope = mutableSetOf<ScopeToken>().apply {
                addAll(
                    clientScope.filter { it.id != refreshTokenScope.get().id && it.id != accessTokenScope.get().id }
                )
                addAll(principal.scope)
            }
        )
    }
}
