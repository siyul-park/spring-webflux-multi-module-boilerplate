package io.github.siyual_park.application.server.helper

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.auth.domain.token.TokenFactory
import io.github.siyual_park.persistence.AsyncLazy
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class AuthorizationHeaderGenerator(
    private val scopeTokenStorage: ScopeTokenStorage,
    private val tokenFactory: TokenFactory
) {
    private val accessTokenScope = AsyncLazy {
        scopeTokenStorage.loadOrFail("access-token:create")
    }
    private val refreshTokenScope = AsyncLazy {
        scopeTokenStorage.loadOrFail("refresh-token:create")
    }

    suspend fun generate(principal: Principal): String {
        val token = tokenFactory.create(
            principal,
            Duration.ofDays(1),
            pop = setOf(accessTokenScope.get(), refreshTokenScope.get())
        )
        return "bearer ${token.signature}"
    }
}
