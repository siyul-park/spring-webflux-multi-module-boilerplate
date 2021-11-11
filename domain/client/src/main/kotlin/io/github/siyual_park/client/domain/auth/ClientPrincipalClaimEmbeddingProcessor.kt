package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.token.ClaimEmbeddingProcessor
import io.github.siyual_park.auth.domain.token.Claims
import org.springframework.stereotype.Component

@Component
class ClientPrincipalClaimEmbeddingProcessor : ClaimEmbeddingProcessor<ClientPrincipal> {
    override val principalClazz = ClientPrincipal::class

    override suspend fun embedding(principal: ClientPrincipal): Claims {
        val claims = Claims()

        claims["cid"] = principal.id
        claims["scope"] = principal.scope

        return claims
    }
}
