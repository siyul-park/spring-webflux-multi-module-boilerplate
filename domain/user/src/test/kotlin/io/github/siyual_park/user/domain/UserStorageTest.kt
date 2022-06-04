package io.github.siyual_park.user.domain

import kotlinx.coroutines.flow.toSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserStorageTest : UserTestHelper() {
    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            scopeTokenFactory.upsert("user:pack")
        }
    }

    @Test
    fun load() = blocking {
        val user1 = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it) }
        val user2 = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it) }

        assertEquals(userStorage.load(user1.id), user1)
        assertEquals(userStorage.load(user2.id), user2)

        assertEquals(userStorage.load(listOf(user1.id, user2.id)).toSet(), setOf(user1, user2))
    }
}
