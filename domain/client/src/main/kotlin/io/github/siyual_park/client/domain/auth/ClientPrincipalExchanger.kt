package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.client.domain.ClientFinder
import io.github.siyual_park.client.domain.ClientScopeFinder
import io.github.siyual_park.client.entity.Client
import io.github.siyual_park.client.entity.ClientEntity
import io.github.siyual_park.reader.finder.findByIdOrFail
import kotlinx.coroutines.flow.toSet
import org.springframework.stereotype.Component

@Component
class ClientPrincipalExchanger(
    private val clientFinder: ClientFinder,
    private val clientScopeFinder: ClientScopeFinder
) {
    suspend fun exchange(client: ClientEntity): ClientPrincipal {
        val scope = clientScopeFinder.findAllWithResolvedByClientId(client.clientId!!).toSet()
        return ClientPrincipal(
            id = client.clientId!!.toString(),
            scope = scope.toSet()
        )
    }

    suspend fun exchange(clientPrincipal: ClientPrincipal): Client {
        return clientFinder.findByIdOrFail(clientPrincipal.clientId)
    }
}
