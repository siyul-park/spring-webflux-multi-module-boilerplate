package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.PrincipalMapping
import io.github.siyual_park.auth.domain.principal_refresher.PrincipalRefreshStrategy
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.client.domain.ClientStorage
import io.github.siyual_park.persistence.loadOrFail
import kotlinx.coroutines.flow.toSet
import org.springframework.stereotype.Component

@Component
@PrincipalMapping(ClientPrincipal::class)
class ClientPrincipalRefreshStrategy(
    private val clientStorage: ClientStorage,
) : PrincipalRefreshStrategy<ClientPrincipal> {

    override suspend fun refresh(principal: ClientPrincipal): ClientPrincipal {
        val client = clientStorage.loadOrFail(principal.clientId)
        val clientScope = client.getScope().toSet()

        return ClientPrincipal(
            id = principal.id,
            clientId = principal.clientId,
            scope = clientScope
        )
    }
}
