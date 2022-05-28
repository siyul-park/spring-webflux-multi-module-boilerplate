package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.user.domain.UserTestHelper
import io.github.siyual_park.user.dummy.DummyCreateUserPayload
import io.github.siyual_park.user.dummy.DummyScopeNameFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UserPrincipalRefreshStrategyTest : UserTestHelper() {
    private val userPrincipalRefreshStrategy = UserPrincipalRefreshStrategy(userStorage)

    @Test
    fun refresh() = blocking {
        val userScope = scopeTokenFactory.upsert("user:pack")
        val customScope = scopeTokenFactory.upsert(DummyScopeNameFactory.create(10))
        userScope.grant(customScope)

        val user = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }
        val principal = UserPrincipal(id = user.id.toString(), scope = setOf())

        val refreshed = userPrincipalRefreshStrategy.refresh(principal)

        assertEquals(refreshed.id, principal.id)
        assertEquals(refreshed.userId, principal.userId)
        assertEquals(refreshed.clientId, principal.clientId)
        assertTrue(refreshed.scope.contains(customScope))
    }
}
