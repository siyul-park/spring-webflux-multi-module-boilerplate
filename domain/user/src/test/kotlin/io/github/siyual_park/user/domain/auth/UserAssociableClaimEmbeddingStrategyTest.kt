package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.user.domain.MockCreateUserPayloadFactory
import io.github.siyual_park.user.domain.UserTestHelper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserAssociableClaimEmbeddingStrategyTest : UserTestHelper() {
    private val userAssociableClaimEmbeddingStrategy = UserAssociableClaimEmbeddingStrategy()

    @Test
    fun embedding() = blocking {
        val user = MockCreateUserPayloadFactory.create()
            .let { userStorage.save(it) }
        val principal = user.toPrincipal()

        val claim = userAssociableClaimEmbeddingStrategy.embedding(principal)

        assertEquals(user.id, claim["uid"])
    }
}
