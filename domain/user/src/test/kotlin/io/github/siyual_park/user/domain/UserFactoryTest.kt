package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFactory
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenMapper
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.auth.migration.CreateScopeRelation
import io.github.siyual_park.auth.migration.CreateScopeToken
import io.github.siyual_park.auth.migration.CreateToken
import io.github.siyual_park.auth.repository.ScopeRelationRepository
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.data.test.DataTestHelper
import io.github.siyual_park.data.test.MongoTestHelper
import io.github.siyual_park.event.EventEmitter
import io.github.siyual_park.user.dummy.DummyCreateUserPayload
import io.github.siyual_park.user.dummy.DummyScopeNameFactory
import io.github.siyual_park.user.migration.CreateUser
import io.github.siyual_park.user.migration.CreateUserCredential
import io.github.siyual_park.user.migration.CreateUserScope
import io.github.siyual_park.user.repository.UserCredentialRepository
import io.github.siyual_park.user.repository.UserRepository
import io.github.siyual_park.user.repository.UserScopeRepository
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import java.util.Optional

class UserFactoryTest : DataTestHelper() {
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
    private val userFactory = UserFactory(userRepository, userCredentialRepository, userMapper, scopeTokenStorage, transactionalOperator, eventEmitter)

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
