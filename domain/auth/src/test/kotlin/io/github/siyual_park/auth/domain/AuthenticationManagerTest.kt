package io.github.siyual_park.auth.domain

import com.github.javafaker.Faker
import io.github.siyual_park.auth.domain.authentication.AllowAllAuthenticateFilter
import io.github.siyual_park.auth.domain.authentication.Authenticator
import io.github.siyual_park.auth.domain.authentication.AuthorizationStrategy
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.exception.AuthorizeException
import io.github.siyual_park.data.test.DataTestHelper
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

class AuthenticationManagerTest : DataTestHelper() {
    internal class TestAuthorizationStrategy(
        type: String,
        private val credentials: String
    ) : AuthorizationStrategy<Principal>(type) {
        override suspend fun authenticate(credentials: String): Principal {
            if (this.credentials != credentials) {
                throw AuthorizeException()
            }

            return object : Principal {
                override val id = ULID.randomULID()
                override var scope = emptySet<ScopeToken>()
            }
        }
    }
    private val faker = Faker()

    private val filter = AllowAllAuthenticateFilter()
    private val authenticator = Authenticator()
    private val authenticationManager = AuthenticationManager(authenticator)

    @Test
    fun authenticate() = blocking {
        val type = faker.random().hex()
        val credentials = faker.random().hex()

        val athentication = UsernamePasswordAuthenticationToken(type, credentials)

        assertThrows<InternalAuthenticationServiceException> {
            authenticationManager.authenticate(athentication).awaitSingleOrNull()
        }

        authenticator.register(filter, TestAuthorizationStrategy(type, credentials))

        val principal = authenticationManager.authenticate(athentication).awaitSingleOrNull()
        assertNotNull(principal)
    }
}
