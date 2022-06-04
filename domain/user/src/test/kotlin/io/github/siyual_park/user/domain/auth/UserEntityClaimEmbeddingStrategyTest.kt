package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.user.domain.MockCreateUserPayloadFactory
import io.github.siyual_park.user.domain.UserTestHelper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserEntityClaimEmbeddingStrategyTest : UserTestHelper() {
    private val userEntityClaimEmbeddingStrategy = UserEntityClaimEmbeddingStrategy()

    @Test
    fun embedding() = blocking {
        val user = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it) }
        val principal = user.toPrincipal()

        val claim = userEntityClaimEmbeddingStrategy.embedding(principal)

        assertEquals(user.id, claim["uid"])
    }
}
