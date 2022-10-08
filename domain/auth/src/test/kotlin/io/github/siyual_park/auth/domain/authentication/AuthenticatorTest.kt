package io.github.siyual_park.auth.domain.authentication

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.exception.AuthorizeException
import io.github.siyual_park.auth.exception.UnauthorizatedException
import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.ulid.ULID
import net.datafaker.Faker
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AuthenticatorTest : CoroutineTestHelper() {
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

    @Test
    fun registerStrategy() {
        val authenticator = Authenticator()
        authenticator.register(filter, TestAuthorizationStrategy("", ""))
    }

    @Test
    fun authenticate() = blocking {
        val authenticator = Authenticator()

        val type = faker.random().hex()
        val credentials = faker.random().hex()

        assertThrows<UnauthorizatedException> { authenticator.authenticate(AuthorizationPayload(type, credentials)) }

        authenticator.register(filter, TestAuthorizationStrategy(type, credentials))
        authenticator.authenticate(AuthorizationPayload(type, credentials))
    }
}
