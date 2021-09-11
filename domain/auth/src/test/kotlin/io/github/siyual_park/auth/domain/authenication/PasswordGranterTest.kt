package io.github.siyual_park.auth.domain.authenication

import io.github.siyual_park.auth.AuthTest
import io.github.siyual_park.auth.domain.ScopeFinder
import io.github.siyual_park.auth.domain.ScopeTokenGenerator
import io.github.siyual_park.auth.domain.UserFactory
import io.github.siyual_park.auth.domain.authenticator.PasswordGrantPayload
import io.github.siyual_park.auth.domain.authenticator.PasswordGranter
import io.github.siyual_park.auth.domain.authenticator.hasScope
import io.github.siyual_park.auth.exception.PasswordIncorrectException
import io.github.siyual_park.auth.factory.CreateUserPayloadFactory
import io.github.siyual_park.auth.factory.ScopeTokenFactory
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.auth.repository.UserCredentialRepository
import io.github.siyual_park.auth.repository.UserRepository
import io.github.siyual_park.auth.repository.UserScopeRepository
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PasswordGranterTest : AuthTest() {
    private val hashAlgorithm = "SHA-256"

    private val userRepository = UserRepository(connectionFactory)
    private val scopeTokenRepository = ScopeTokenRepository(connectionFactory)
    private val userCredentialRepository = UserCredentialRepository(connectionFactory)
    private val userScopeRepository = UserScopeRepository(connectionFactory)

    private val scopeTokenGenerator = ScopeTokenGenerator(scopeTokenRepository)
    private val userFactory = UserFactory(
        userRepository,
        scopeTokenRepository,
        userCredentialRepository,
        userScopeRepository,
        transactionalOperator,
        hashAlgorithm
    )

    private val scopeFinder = ScopeFinder(
        scopeTokenRepository,
        userScopeRepository
    )

    private val passwordGranter = PasswordGranter(
        userRepository,
        userCredentialRepository,
        scopeFinder
    )

    private val createUserPayloadFactory = CreateUserPayloadFactory()
    private val scopeTokenFactory = ScopeTokenFactory()

    init {
        scopeTokenGenerator.register(scopeTokenFactory.create(true))
        scopeTokenGenerator.register(scopeTokenFactory.create(true))
        scopeTokenGenerator.register(scopeTokenFactory.create(false))
        scopeTokenGenerator.register(scopeTokenFactory.create(false))
    }

    @BeforeEach
    fun generateScopeToken() = blocking {
        scopeTokenGenerator.generate()
    }

    @Test
    fun testAuthenticateSuccess() = blocking {
        val createUserPayload = createUserPayloadFactory.create()
        val user = userFactory.create(createUserPayload)
        val passwordGrantPayload = PasswordGrantPayload(
            username = createUserPayload.username,
            password = createUserPayload.password
        )

        val authentication = passwordGranter.authenticate(passwordGrantPayload)

        assertEquals(user.id, authentication.id)

        val scope = scopeFinder.find(user).toList()
        assertEquals(scope.size, authentication.scope.size)
        assert(authentication.hasScope(scope))
    }

    @Test
    fun testAuthenticateFail() = blocking {
        val createUserPayload = createUserPayloadFactory.create()
        userFactory.create(createUserPayload)

        val otherUserPayload = createUserPayloadFactory.create()
        val passwordGrantPayload = PasswordGrantPayload(
            username = createUserPayload.username,
            password = otherUserPayload.password
        )

        assertThrows<PasswordIncorrectException> {
            passwordGranter.authenticate(passwordGrantPayload)
        }
    }
}
