package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.authorization.ClaimMapping
import io.github.siyual_park.auth.domain.token.ClaimEmbeddingStrategy
import io.github.siyual_park.client.entity.ClientEntity
import org.springframework.stereotype.Component

@Component
@ClaimMapping(ClientEntity::class)
class ClientEntityClaimEmbeddingStrategy : ClaimEmbeddingStrategy<ClientEntity> {
    override val clazz = ClientEntity::class

    override suspend fun embedding(principal: ClientEntity): Map<String, Any> {
        val claims = mutableMapOf<String, Any>()

        val clientId = principal.clientId
        if (clientId != null) {
            claims["cid"] = clientId
        }

        return claims
    }
}
