package io.github.siyual_park.auth.domain

import io.github.siyual_park.auth.AuthTest
import io.github.siyual_park.auth.factory.CreateUserPayloadFactory
import io.github.siyual_park.auth.factory.ScopeTokenFactory
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.auth.repository.UserCredentialRepository
import io.github.siyual_park.auth.repository.UserRepository
import io.github.siyual_park.auth.repository.UserScopeRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.MessageDigest

class UserFactoryTest : AuthTest() {
    private val hashAlgorithm = "SHA-256"

    private val userRepository = UserRepository(connectionFactory)
    private val scopeTokenRepository = ScopeTokenRepository(connectionFactory)
    private val userAuthInfoRepository = UserCredentialRepository(connectionFactory)
    private val userScopeRepository = UserScopeRepository(connectionFactory)

    private val scopeTokenGenerator = ScopeTokenGenerator(scopeTokenRepository)
    private val userFactory = UserFactory(
        userRepository,
        scopeTokenRepository,
        userAuthInfoRepository,
        userScopeRepository,
        transactionalOperator,
        hashAlgorithm
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
    fun create() = blocking {
        val payload = createUserPayloadFactory.create()
        val user = userFactory.create(payload)

        assertNotNull(user.id)
        assertEquals(user.name, payload.username)

        val userAuthInfo = userAuthInfoRepository.findByUser(user)

        assertNotNull(userAuthInfo?.id)
        assertEquals(userAuthInfo?.userId, user.id)
        assertEquals(userAuthInfo?.hashAlgorithm, hashAlgorithm)

        val messageDigest = MessageDigest.getInstance(hashAlgorithm)
        val password = messageDigest.hash(payload.password)

        assertEquals(userAuthInfo?.password, password)

        val defaultScopeTokens = scopeTokenRepository.findAllByDefault(true).toList()

        val userScopeTokens = userScopeRepository.findAllByUser(user)
            .map { scopeTokenRepository.findById(it.scopeTokenId) }
            .toList()

        assertEquals(defaultScopeTokens.size, userScopeTokens.size)
    }
}
