package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.PrincipalMapping
import io.github.siyual_park.auth.domain.token.ClaimEmbeddingProcessor
import io.github.siyual_park.auth.domain.token.Claims
import org.springframework.stereotype.Component

@Component
@PrincipalMapping(UserPrincipal::class)
class UserPrincipalClaimEmbeddingProcessor : ClaimEmbeddingProcessor<UserPrincipal> {
    override suspend fun embedding(principal: UserPrincipal): Claims {
        val claims = Claims()

        claims["uid"] = principal.id
        if (principal.clientId != null) {
            claims["cid"] = principal.clientId
        }
        claims["scope"] = principal.scope

        return claims
    }
}
