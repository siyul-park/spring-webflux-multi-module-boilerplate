package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.user.dummy.DummyCreateUserPayload
import io.github.siyual_park.user.dummy.DummyScopeNameFactory
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
        val payload = DummyCreateUserPayload.create()
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
        val customScope = scopeTokenFactory.upsert(DummyScopeNameFactory.create(10))

        val payload = DummyCreateUserPayload.create(
            DummyCreateUserPayload.Template(
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
