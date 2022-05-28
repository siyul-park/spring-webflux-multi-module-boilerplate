package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.user.domain.UserTestHelper
import io.github.siyual_park.user.dummy.DummyCreateUserPayload
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserPrincipalClaimEmbeddingStrategyTest : UserTestHelper() {
    private val userPrincipalClaimEmbeddingStrategy = UserPrincipalClaimEmbeddingStrategy()

    @Test
    fun embedding() = blocking {
        val user = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }
        val principal = user.toPrincipal()

        val claim = userPrincipalClaimEmbeddingStrategy.embedding(principal)

        assertEquals(user.id.toString(), claim["uid"])
        assertEquals(null, claim["cid"])
    }
}
