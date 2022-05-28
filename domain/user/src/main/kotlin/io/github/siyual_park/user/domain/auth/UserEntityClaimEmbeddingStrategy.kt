package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.authorization.ClaimMapping
import io.github.siyual_park.auth.domain.token.ClaimEmbeddingStrategy
import io.github.siyual_park.user.entity.UserEntity
import org.springframework.stereotype.Component

@Component
@ClaimMapping(UserEntity::class)
class UserEntityClaimEmbeddingStrategy : ClaimEmbeddingStrategy<UserEntity> {
    override val clazz = UserEntity::class

    override suspend fun embedding(principal: UserEntity): Map<String, Any> {
        val claims = mutableMapOf<String, Any>()

        val userId = principal.userId
        if (userId != null) {
            claims["uid"] = userId
        }

        return claims
    }
}
