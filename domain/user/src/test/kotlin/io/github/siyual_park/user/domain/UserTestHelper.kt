package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenMapper
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.migration.CreateScopeRelation
import io.github.siyual_park.auth.migration.CreateScopeToken
import io.github.siyual_park.auth.migration.CreateToken
import io.github.siyual_park.auth.repository.ScopeRelationEntityRepository
import io.github.siyual_park.auth.repository.ScopeTokenEntityRepository
import io.github.siyual_park.data.test.DataTestHelper
import io.github.siyual_park.data.test.MongoTestHelper
import io.github.siyual_park.user.migration.CreateUser
import io.github.siyual_park.user.migration.CreateUserScope
import io.github.siyual_park.user.repository.UserEntityRepository
import io.github.siyual_park.user.repository.UserScopeEntityRepository
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

    protected val scopeRelationEntityRepository = ScopeRelationEntityRepository(entityOperations)
    protected val scopeTokenEntityRepository = ScopeTokenEntityRepository(entityOperations)
    protected val userEntityRepository = UserEntityRepository(entityOperations)
    protected val userScopeEntityRepository = spyk(UserScopeEntityRepository(entityOperations))

    protected val scopeTokenMapper = ScopeTokenMapper(scopeTokenEntityRepository, scopeRelationEntityRepository)
    protected val scopeTokenStorage = ScopeTokenStorage(scopeTokenEntityRepository, scopeTokenMapper)

    protected val userStorage = UserStorage(userEntityRepository, userScopeEntityRepository, scopeTokenStorage)

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
