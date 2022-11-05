package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenMapper
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.migration.CreateScopeRelation
import io.github.siyual_park.auth.migration.CreateScopeToken
import io.github.siyual_park.auth.migration.CreateToken
import io.github.siyual_park.auth.repository.ScopeRelationDataRepository
import io.github.siyual_park.auth.repository.ScopeTokenDataRepository
import io.github.siyual_park.data.test.DataTestHelper
import io.github.siyual_park.data.test.MongoTestHelper
import io.github.siyual_park.user.migration.CreateUser
import io.github.siyual_park.user.migration.CreateUserScope
import io.github.siyual_park.user.repository.UserDataRepository
import io.github.siyual_park.user.repository.UserScopeDataRepository
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
            .register(CreateUserScope(entityOperations))
    }

    protected val scopeRelationDataRepository = ScopeRelationDataRepository(entityOperations)
    protected val scopeTokenDataRepository = ScopeTokenDataRepository(entityOperations)
    protected val userDataRepository = UserDataRepository(entityOperations)
    protected val userScopeDataRepository = spyk(UserScopeDataRepository(entityOperations))

    protected val scopeTokenMapper = ScopeTokenMapper(scopeTokenDataRepository, scopeRelationDataRepository)
    protected val scopeTokenStorage = ScopeTokenStorage(scopeTokenDataRepository, scopeTokenMapper)

    protected val userStorage = UserStorage(userDataRepository, userScopeDataRepository, scopeTokenStorage)

    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            scopeTokenStorage.upsert("user:pack")
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
