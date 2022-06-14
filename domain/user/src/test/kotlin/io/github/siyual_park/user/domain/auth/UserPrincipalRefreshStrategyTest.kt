package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.scope_token.MockScopeNameFactory
import io.github.siyual_park.user.domain.MockCreateUserPayloadFactory
import io.github.siyual_park.user.domain.UserTestHelper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UserPrincipalRefreshStrategyTest : UserTestHelper() {
    private val userPrincipalRefreshStrategy = UserPrincipalRefreshStrategy(userStorage)

    @Test
    fun refresh() = blocking {
        val userScope = scopeTokenFactory.upsert("user:pack")
        val customScope = scopeTokenFactory.upsert(MockScopeNameFactory.create())
        userScope.grant(customScope)

        val user = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it) }
        val principal = UserPrincipal(userId = user.id, scope = setOf())

        val refreshed = userPrincipalRefreshStrategy.refresh(principal)

        assertEquals(refreshed.id, principal.id)
        assertEquals(refreshed.userId, principal.userId)
        assertEquals(refreshed.clientId, principal.clientId)
        assertTrue(refreshed.scope.contains(customScope))
    }
}
