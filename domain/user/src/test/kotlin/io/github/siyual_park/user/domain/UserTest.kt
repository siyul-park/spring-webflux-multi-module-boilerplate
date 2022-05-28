package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFactory
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenMapper
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.migration.CreateScopeRelation
import io.github.siyual_park.auth.migration.CreateScopeToken
import io.github.siyual_park.auth.migration.CreateToken
import io.github.siyual_park.auth.repository.ScopeRelationRepository
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.data.test.DataTestHelper
import io.github.siyual_park.data.test.MongoTestHelper
import io.github.siyual_park.event.EventEmitter
import io.github.siyual_park.persistence.loadOrFail
import io.github.siyual_park.user.dummy.DummyCreateUserPayload
import io.github.siyual_park.user.dummy.DummyScopeNameFactory
import io.github.siyual_park.user.migration.CreateUser
import io.github.siyual_park.user.migration.CreateUserCredential
import io.github.siyual_park.user.migration.CreateUserScope
import io.github.siyual_park.user.repository.UserCredentialRepository
import io.github.siyual_park.user.repository.UserRepository
import io.github.siyual_park.user.repository.UserScopeRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import java.util.Optional

class UserTest : DataTestHelper() {
    init {
        migrationManager
            .register(CreateScopeToken(entityOperations))
            .register(CreateScopeRelation(entityOperations))
            .register(CreateToken(mongoTemplate))
            .register(CreateUser(entityOperations, mongoTemplate))
            .register(CreateUserCredential(entityOperations))
            .register(CreateUserScope(entityOperations))
    }

    private val eventEmitter = EventEmitter()

    private val scopeRelationRepository = ScopeRelationRepository(entityOperations)
    private val scopeTokenRepository = ScopeTokenRepository(entityOperations)
    private val userRepository = UserRepository(entityOperations, eventEmitter)
    private val userCredentialRepository = UserCredentialRepository(entityOperations, eventEmitter)
    private val userScopeRepository = UserScopeRepository(entityOperations, eventEmitter)

    private val scopeTokenMapper = ScopeTokenMapper(
        scopeTokenRepository,
        scopeRelationRepository,
        transactionalOperator,
        eventEmitter
    )
    private val scopeTokenStorage = ScopeTokenStorage(scopeTokenRepository, scopeTokenMapper)

    private val scopeTokenFactory = ScopeTokenFactory(scopeTokenRepository, scopeTokenMapper, eventEmitter)

    private val userMapper = UserMapper(userRepository, userCredentialRepository, userScopeRepository, scopeTokenStorage, transactionalOperator, eventEmitter)
    private val usersMapper = UsersMapper(userRepository, userCredentialRepository, userScopeRepository, scopeTokenStorage, transactionalOperator, eventEmitter)

    private val userFactory = UserFactory(userRepository, userCredentialRepository, userMapper, scopeTokenStorage, transactionalOperator, eventEmitter)
    private val userStorage = UserStorage(userRepository, userMapper, usersMapper)

    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            scopeTokenFactory.upsert("user:pack")
        }
    }

    @Test
    fun sync() = blocking {
        val user = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        val other = DummyCreateUserPayload.create()

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
        val customScope = scopeTokenFactory.upsert(DummyScopeNameFactory.create(10))

        val user1 = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }
        val user2 = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        val users = userStorage.load(listOf(user1.id, user2.id)).toList()
        val loadedUser1 = users.find { it.id == user1.id }
        val loadedUser2 = users.find { it.id == user2.id }

        assertEquals(loadedUser1, user1)
        assertEquals(loadedUser2, user2)

        assertEquals(loadedUser1?.getScope(deep = false)?.toSet(), user1.getScope(deep = false).toSet())
        assertEquals(loadedUser1?.getScope(deep = true)?.toSet(), user1.getScope(deep = true).toSet())
        assertEquals(loadedUser2?.getScope(deep = false)?.toSet(), user2.getScope(deep = false).toSet())
        assertEquals(loadedUser2?.getScope(deep = true)?.toSet(), user2.getScope(deep = true).toSet())

        loadedUser1?.grant(customScope)

        assertEquals(loadedUser1?.getScope(deep = false)?.toSet(), user1.getScope(deep = false).toSet())
        assertEquals(loadedUser1?.getScope(deep = true)?.toSet(), user1.getScope(deep = true).toSet())
        assertEquals(loadedUser2?.getScope(deep = false)?.toSet(), user2.getScope(deep = false).toSet())
        assertEquals(loadedUser2?.getScope(deep = true)?.toSet(), user2.getScope(deep = true).toSet())

        loadedUser2?.grant(customScope)

        assertEquals(loadedUser1?.getScope(deep = false)?.toSet(), user1.getScope(deep = false).toSet())
        assertEquals(loadedUser1?.getScope(deep = true)?.toSet(), user1.getScope(deep = true).toSet())
        assertEquals(loadedUser2?.getScope(deep = false)?.toSet(), user2.getScope(deep = false).toSet())
        assertEquals(loadedUser2?.getScope(deep = true)?.toSet(), user2.getScope(deep = true).toSet())
    }

    @Test
    fun revoke() = blocking {
        val customScope = scopeTokenFactory.upsert(DummyScopeNameFactory.create(10))
        val template = DummyCreateUserPayload.Template(
            scope = Optional.of(listOf(customScope))
        )

        val user1 = DummyCreateUserPayload.create(template)
            .let { userFactory.create(it) }
        val user2 = DummyCreateUserPayload.create(template)
            .let { userFactory.create(it) }

        val users = userStorage.load(listOf(user1.id, user2.id)).toList()
        val loadedUser1 = users.find { it.id == user1.id }
        val loadedUser2 = users.find { it.id == user2.id }

        assertEquals(loadedUser1, user1)
        assertEquals(loadedUser2, user2)

        assertEquals(loadedUser1?.getScope(deep = false)?.toSet(), user1.getScope(deep = false).toSet())
        assertEquals(loadedUser1?.getScope(deep = true)?.toSet(), user1.getScope(deep = true).toSet())
        assertEquals(loadedUser2?.getScope(deep = false)?.toSet(), user2.getScope(deep = false).toSet())
        assertEquals(loadedUser2?.getScope(deep = true)?.toSet(), user2.getScope(deep = true).toSet())

        loadedUser1?.revoke(customScope)

        assertEquals(loadedUser1?.getScope(deep = false)?.toSet(), user1.getScope(deep = false).toSet())
        assertEquals(loadedUser1?.getScope(deep = true)?.toSet(), user1.getScope(deep = true).toSet())
        assertEquals(loadedUser2?.getScope(deep = false)?.toSet(), user2.getScope(deep = false).toSet())
        assertEquals(loadedUser2?.getScope(deep = true)?.toSet(), user2.getScope(deep = true).toSet())

        loadedUser2?.revoke(customScope)

        assertEquals(loadedUser1?.getScope(deep = false)?.toSet(), user1.getScope(deep = false).toSet())
        assertEquals(loadedUser1?.getScope(deep = true)?.toSet(), user1.getScope(deep = true).toSet())
        assertEquals(loadedUser2?.getScope(deep = false)?.toSet(), user2.getScope(deep = false).toSet())
        assertEquals(loadedUser2?.getScope(deep = true)?.toSet(), user2.getScope(deep = true).toSet())
    }

    companion object {
        private val helper = MongoTestHelper()

        val mongoTemplate: ReactiveMongoTemplate
            get() = helper.mongoTemplate

        @BeforeAll
        @JvmStatic
        fun setUpAll() = helper.setUp()

        @AfterAll
        @JvmStatic
        fun tearDownAll() = helper.tearDown()
    }
}
