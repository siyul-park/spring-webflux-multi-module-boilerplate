package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.scope_token.MockScopeNameFactory
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

class UserFactoryTest : UserTestHelper() {
    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            scopeTokenFactory.upsert("user:pack")
        }
    }

    @Test
    fun `create, when use default`() = blocking {
        val payload = MockCreateUserPayloadFactory.create()
        val user = userFactory.create(payload)

        assertEquals(payload.name, user.name)
        assertEquals(payload.email, user.email)
        assertTrue(user.getCredential().isPassword(payload.password))

        val scope = user.getScope(deep = false).toList()
        assertEquals(1, scope.size)
        assertEquals(scopeTokenStorage.loadOrFail("user:pack"), scope[0])
    }

    @Test
    fun `create, when use custom scope`() = blocking {
        val customScope = scopeTokenFactory.upsert(MockScopeNameFactory.create())

        val payload = MockCreateUserPayloadFactory.create(
            MockCreateUserPayloadFactory.Template(
                scope = Optional.of(listOf(customScope))
            )
        )
        val user = userFactory.create(payload)

        assertEquals(payload.name, user.name)
        assertEquals(payload.email, user.email)
        assertTrue(user.getCredential().isPassword(payload.password))

        val scope = user.getScope(deep = false).toList()
        assertEquals(1, scope.size)
        assertEquals(customScope, scope[0])
    }
}
