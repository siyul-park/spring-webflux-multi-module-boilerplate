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
import io.github.siyual_park.user.migration.CreateUser
import io.github.siyual_park.user.migration.CreateUserCredential
import io.github.siyual_park.user.migration.CreateUserScope
import io.github.siyual_park.user.repository.UserCredentialRepository
import io.github.siyual_park.user.repository.UserRepository
import io.github.siyual_park.user.repository.UserScopeRepository
import io.mockk.spyk
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.core.ReactiveMongoTemplate

abstract class UserTestHelper(
    converters: Collection<Converter<*, *>> = emptyList()
) : DataTestHelper(converters) {
    init {
        migrationManager
            .register(CreateScopeToken(entityOperations))
            .register(CreateScopeRelation(entityOperations))
            .register(CreateToken(mongoTemplate))
            .register(CreateUser(entityOperations, mongoTemplate))
            .register(CreateUserCredential(entityOperations))
            .register(CreateUserScope(entityOperations))
    }

    protected val scopeRelationRepository = ScopeRelationRepository(entityOperations)
    protected val scopeTokenRepository = ScopeTokenRepository(entityOperations)
    protected val userRepository = UserRepository(entityOperations)
    protected val userCredentialRepository = UserCredentialRepository(entityOperations)
    protected val userScopeRepository = spyk(UserScopeRepository(entityOperations))

    protected val scopeTokenMapper = ScopeTokenMapper(scopeTokenRepository, scopeRelationRepository)
    protected val scopeTokenStorage = ScopeTokenStorage(scopeTokenRepository, scopeTokenMapper)
    protected val scopeTokenFactory = ScopeTokenFactory(scopeTokenRepository, scopeTokenMapper)

    protected val userFactory = UserFactory(userRepository, userCredentialRepository, userScopeRepository, scopeTokenStorage)
    protected val userStorage = UserStorage(userRepository, userCredentialRepository, userScopeRepository, scopeTokenStorage)

    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            scopeTokenFactory.upsert("user:pack")
        }
    }

    companion object {
        val helper = MongoTestHelper()

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
