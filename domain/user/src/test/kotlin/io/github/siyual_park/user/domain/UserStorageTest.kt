package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.scope_token.MockScopeNameFactory
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

class UserStorageTest : UserTestHelper() {
    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            scopeTokenStorage.upsert("user:pack")
        }
    }

    @Test
    fun `save, when use default`() = blocking {
        val payload = MockCreateUserPayloadFactory.create()
        val user = userStorage.save(payload)

        assertEquals(payload.name, user.name)
        assertEquals(payload.email, user.email)
        Assertions.assertTrue(user.getCredential().check(payload.password))

        val scope = user.getScope(deep = false).toList()
        assertEquals(1, scope.size)
        assertEquals(scopeTokenStorage.loadOrFail("user:pack"), scope[0])
    }

    @Test
    fun `save, when use custom scope`() = blocking {
        val customScope = scopeTokenStorage.upsert(MockScopeNameFactory.create())

        val payload = MockCreateUserPayloadFactory.create(
            MockCreateUserPayloadFactory.Template(
                scope = Optional.of(listOf(customScope))
            )
        )
        val user = userStorage.save(payload)

        assertEquals(payload.name, user.name)
        assertEquals(payload.email, user.email)
        Assertions.assertTrue(user.getCredential().check(payload.password))

        val scope = user.getScope(deep = false).toList()
        assertEquals(1, scope.size)
        assertEquals(customScope, scope[0])
    }

    @Test
    fun load() = blocking {
        val user1 = MockCreateUserPayloadFactory.create()
            .let { userStorage.save(it) }
        val user2 = MockCreateUserPayloadFactory.create()
            .let { userStorage.save(it) }

        assertEquals(userStorage.load(user1.id), user1)
        assertEquals(userStorage.load(user2.id), user2)

        assertEquals(userStorage.load(listOf(user1.id, user2.id)).toSet(), setOf(user1, user2))
    }
}
