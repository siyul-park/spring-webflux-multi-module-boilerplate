package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.PrincipalMapping
import io.github.siyual_park.auth.domain.token.ClaimEmbeddingStrategy
import org.springframework.stereotype.Component

@Component
@PrincipalMapping(ClientPrincipal::class)
class ClientPrincipalClaimEmbeddingStrategy : ClaimEmbeddingStrategy<ClientPrincipal> {
    override suspend fun embedding(principal: ClientPrincipal): Map<String, Any> {
        val claims = mutableMapOf<String, Any>()

        claims["cid"] = principal.clientId

        return claims
    }
}
