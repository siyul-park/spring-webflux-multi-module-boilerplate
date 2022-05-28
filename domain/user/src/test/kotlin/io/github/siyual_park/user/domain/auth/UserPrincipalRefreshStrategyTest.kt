package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.user.domain.UserTestHelper
import io.github.siyual_park.user.dummy.DummyCreateUserPayload
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserPrincipalRefreshStrategyTest : UserTestHelper() {
    private val userPrincipalRefreshStrategy = UserPrincipalRefreshStrategy(
        userStorage, scopeTokenStorage
    )

    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            val accessToken = scopeTokenFactory.upsert("access-token:create")
            val refreshToken = scopeTokenFactory.upsert("refresh-token:create")

            val user = scopeTokenFactory.upsert("user:pack")

            user.grant(accessToken)
            user.grant(refreshToken)
        }
    }

    @Test
    fun refresh() = blocking {
        val accessToken = scopeTokenFactory.upsert("access-token:create")
        val refreshToken = scopeTokenFactory.upsert("refresh-token:create")

        val user = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }
        val principal = user.toPrincipal()

        val refreshed = userPrincipalRefreshStrategy.refresh(principal)

        assertEquals(refreshed.id, principal.id)
        assertEquals(refreshed.userId, principal.userId)
        assertEquals(refreshed.clientId, principal.clientId)

        assertTrue(principal.scope.contains(accessToken))
        assertTrue(principal.scope.contains(refreshToken))
        assertFalse(refreshed.scope.contains(accessToken))
        assertFalse(refreshed.scope.contains(refreshToken))
    }
}
