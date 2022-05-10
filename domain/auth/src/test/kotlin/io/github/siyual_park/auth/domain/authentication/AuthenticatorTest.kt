package io.github.siyual_park.auth.domain.authentication

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.dummy.DummyStringFactory
import io.github.siyual_park.auth.exception.AuthorizeException
import io.github.siyual_park.auth.exception.UnauthorizatedException
import io.github.siyual_park.coroutine.test.CoroutineTestHelper
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
                override val id = ""
                override var scope = emptySet<ScopeToken>()
            }
        }
    }

    internal class TestAuthenticatePipeline : AuthenticatePipeline<Principal> {
        override val clazz = Principal::class

        override suspend fun pipe(principal: Principal): Principal {
            return principal
        }
    }

    private val filter = AllowAllAuthenticateFilter()

    @Test
    fun registerStrategy() {
        val authenticator = Authenticator()
        authenticator.register(filter, TestAuthorizationStrategy("", ""))
    }

    @Test
    fun registerPipeline() {
        val authenticator = Authenticator()
        authenticator.register(filter, TestAuthenticatePipeline())
    }

    @Test
    fun authenticate() = blocking {
        val authenticator = Authenticator()

        val type = DummyStringFactory.create(10)
        val credentials = DummyStringFactory.create(10)

        assertThrows<UnauthorizatedException> { authenticator.authenticate(AuthorizationPayload(type, credentials)) }

        authenticator.register(filter, TestAuthorizationStrategy(type, credentials))
        authenticator.authenticate(AuthorizationPayload(type, credentials))
    }
}
