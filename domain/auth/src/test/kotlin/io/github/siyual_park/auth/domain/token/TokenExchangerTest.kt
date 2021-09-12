package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.AuthTest
import io.github.siyual_park.auth.domain.ScopeFinder
import io.github.siyual_park.auth.domain.ScopeTokenGenerator
import io.github.siyual_park.auth.domain.UserFactory
import io.github.siyual_park.auth.domain.authenticator.PasswordGrantAuthenticator
import io.github.siyual_park.auth.domain.authenticator.UserAuthenticationExchanger
import io.github.siyual_park.auth.domain.authenticator.payload.PasswordGrantPayload
import io.github.siyual_park.auth.factory.CreateUserPayloadFactory
import io.github.siyual_park.auth.factory.ScopeTokenFactory
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.auth.repository.UserCredentialRepository
import io.github.siyual_park.auth.repository.UserRepository
import io.github.siyual_park.auth.repository.UserScopeRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

class TokenExchangerTest : AuthTest() {
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
    private val userAuthenticationExchanger = UserAuthenticationExchanger(
        userRepository,
        scopeFinder
    )
    private val passwordGrantAuthenticator = PasswordGrantAuthenticator(
        userRepository,
        userCredentialRepository,
        userAuthenticationExchanger
    )
    private val secretGenerator = SecretGenerator()
    private val tokenExchanger = TokenExchanger(secretGenerator.generate(256))

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
    fun testTokenGenerate() = blocking {
        val createUserPayload = createUserPayloadFactory.create()
        userFactory.create(createUserPayload)
        val passwordGrantPayload = PasswordGrantPayload(
            username = createUserPayload.username,
            password = createUserPayload.password
        )

        val authentication = passwordGrantAuthenticator.authenticate(passwordGrantPayload)

        tokenExchanger.encoding(authentication, Duration.ofMinutes(30))
    }
}
