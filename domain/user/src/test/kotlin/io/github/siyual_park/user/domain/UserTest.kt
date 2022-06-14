package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.scope_token.MockScopeNameFactory
import io.github.siyual_park.persistence.loadOrFail
import io.mockk.coVerify
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

class UserTest : UserTestHelper() {
    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            scopeTokenFactory.upsert("user:pack")
        }
    }

    @Test
    fun sync() = blocking {
        val user = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it) }

        val other = MockCreateUserPayloadFactory.create()

        user.name = other.name
        user.email = other.email

        assertEquals(other.name, user.name)
        assertEquals(other.email, user.email)

        user.sync()

        assertEquals(other.name, user.name)
        assertEquals(other.email, user.email)

        val exist = userStorage.loadOrFail(user.id)

        assertEquals(other.name, exist.name)
        assertEquals(other.email, exist.email)
    }

    @Test
    fun grant() = blocking {
        val customScope = scopeTokenFactory.upsert(MockScopeNameFactory.create())

        val user1 = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it) }
        val user2 = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it) }

        val users = userStorage.load(listOf(user1.id, user2.id)).toList()

        val loaded1 = users.find { it.id == user1.id }
        val loaded2 = users.find { it.id == user2.id }

        assertNotNull(loaded1)
        assertNotNull(loaded2)

        loaded1?.grant(customScope)
        assertTrue(loaded1?.has(customScope) == true)
        loaded1?.getScope(deep = false)?.toSet()?.also {
            assertTrue(it.contains(customScope))
        }

        coVerify(exactly = 1) { userScopeRepository.findAll(any()) }

        loaded2?.grant(customScope)
        assertTrue(loaded2?.has(customScope) == true)
        loaded2?.getScope(deep = false)?.toSet()?.also {
            assertTrue(it.contains(customScope))
        }

        coVerify(exactly = 2) { userScopeRepository.findAll(any()) }
    }

    @Test
    fun revoke() = blocking {
        val customScope = scopeTokenFactory.upsert(MockScopeNameFactory.create())
        val template = MockCreateUserPayloadFactory.Template(
            scope = Optional.of(listOf(customScope))
        )

        val user1 = MockCreateUserPayloadFactory.create(template)
            .let { userFactory.create(it) }
        val user2 = MockCreateUserPayloadFactory.create(template)
            .let { userFactory.create(it) }

        val users = userStorage.load(listOf(user1.id, user2.id)).toList()

        val loaded1 = users.find { it.id == user1.id }
        val loaded2 = users.find { it.id == user2.id }

        assertNotNull(loaded1)
        assertNotNull(loaded2)

        loaded1?.revoke(customScope)
        assertTrue(loaded1?.has(customScope) == false)
        loaded1?.getScope(deep = false)?.toSet()?.also {
            assertFalse(it.contains(customScope))
        }

        coVerify(exactly = 1) { userScopeRepository.findAll(any()) }

        loaded2?.revoke(customScope)
        assertTrue(loaded2?.has(customScope) == false)
        loaded2?.getScope(deep = false)?.toSet()?.also {
            assertFalse(it.contains(customScope))
        }

        coVerify(exactly = 2) { userScopeRepository.findAll(any()) }
    }

    @Test
    fun getScope() = blocking {
        val customScope = scopeTokenFactory.upsert(MockScopeNameFactory.create())
        val template = MockCreateUserPayloadFactory.Template(
            scope = Optional.of(listOf(customScope))
        )

        val user1 = MockCreateUserPayloadFactory.create(template)
            .let { userFactory.create(it) }
        val user2 = MockCreateUserPayloadFactory.create(template)
            .let { userFactory.create(it) }

        val users = userStorage.load(listOf(user1.id, user2.id)).toList()

        val loaded1 = users.find { it.id == user1.id }
        val loaded2 = users.find { it.id == user2.id }

        assertNotNull(loaded1)
        assertNotNull(loaded2)

        assertEquals(loaded1?.getScope(deep = false)?.toSet(), user1.getScope(deep = false).toSet())
        assertEquals(loaded2?.getScope(deep = false)?.toSet(), user2.getScope(deep = false).toSet())

        coVerify(exactly = 3) { userScopeRepository.findAll(any()) }

        assertEquals(loaded1?.getScope(deep = true)?.toSet(), user1.getScope(deep = true).toSet())
        assertEquals(loaded2?.getScope(deep = true)?.toSet(), user2.getScope(deep = true).toSet())

        coVerify(exactly = 6) { userScopeRepository.findAll(any()) }

        loaded1?.getScope(deep = false)?.toSet()
        loaded1?.getScope(deep = false)?.toSet()

        coVerify(exactly = 8) { userScopeRepository.findAll(any()) }

        loaded2?.getScope(deep = false)?.toSet()
        loaded2?.getScope(deep = false)?.toSet()

        coVerify(exactly = 9) { userScopeRepository.findAll(any()) }
    }

    @Test
    fun clear() = blocking {
        val user = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it) }

        user.clear()
        assertNull(userStorage.load(user.id))
    }

    @Test
    fun toPrincipal() = blocking {
        val user = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it) }
        val principal = user.toPrincipal()

        assertEquals(user.id, principal.id)
        assertEquals(user.id, principal.userId)
        assertEquals(user.getScope(deep = true).toSet(), principal.scope)
    }
}
