package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.authorization.ClaimMapping
import io.github.siyual_park.auth.domain.token.ClaimEmbeddingStrategy
import io.github.siyual_park.client.entity.ClientAssociable
import org.springframework.stereotype.Component

@Component
@ClaimMapping(ClientAssociable::class)
class ClientAssociableClaimEmbeddingStrategy : ClaimEmbeddingStrategy<ClientAssociable> {
    override val clazz = ClientAssociable::class

    override suspend fun embedding(principal: ClientAssociable): Map<String, Any> {
        val claims = mutableMapOf<String, Any>()

        val clientId = principal.clientId
        if (clientId != null) {
            claims["cid"] = clientId
        }

        return claims
    }
}
