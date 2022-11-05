package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.authorization.ClaimMapping
import io.github.siyual_park.auth.domain.token.ClaimEmbeddingStrategy
import io.github.siyual_park.user.entity.UserAssociable
import org.springframework.stereotype.Component

@Component
@ClaimMapping(UserAssociable::class)
class UserAssociableClaimEmbeddingStrategy : ClaimEmbeddingStrategy<UserAssociable> {
    override val clazz = UserAssociable::class

    override suspend fun embedding(principal: UserAssociable): Map<String, Any> {
        val claims = mutableMapOf<String, Any>()

        val userId = principal.userId
        if (userId != null) {
            claims["uid"] = userId
        }

        return claims
    }
}
