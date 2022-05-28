package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.PrincipalMapping
import io.github.siyual_park.auth.domain.token.ClaimEmbeddingStrategy
import org.springframework.stereotype.Component

@Component
@PrincipalMapping(UserPrincipal::class)
class UserPrincipalClaimEmbeddingStrategy : ClaimEmbeddingStrategy<UserPrincipal> {
    override suspend fun embedding(principal: UserPrincipal): Map<String, Any> {
        val claims = mutableMapOf<String, Any>()

        claims["uid"] = principal.userId
        if (principal.clientId != null) {
            claims["cid"] = principal.clientId
        }

        return claims
    }
}
