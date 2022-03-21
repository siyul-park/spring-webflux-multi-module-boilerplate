package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.PrincipalMapping
import io.github.siyual_park.auth.domain.token.ClaimEmbeddingStrategy
import io.github.siyual_park.auth.domain.token.Claims
import org.springframework.stereotype.Component

@Component
@PrincipalMapping(ClientPrincipal::class)
class ClientPrincipalClaimEmbeddingStrategy : ClaimEmbeddingStrategy<ClientPrincipal> {
    override suspend fun embedding(principal: ClientPrincipal): Claims {
        val claims = Claims()

        claims["cid"] = principal.id
        claims["scope"] = principal.scope

        return claims
    }
}
