package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.hasScope
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFinder
import io.github.siyual_park.auth.exception.RequiredPermissionException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class TokenIssuer(
    private val scopeTokenFinder: ScopeTokenFinder,
    private val tokenEncoder: TokenEncoder,
    private val claimEmbedder: ClaimEmbedder,
    @Value("#{T(java.time.Duration).ofSeconds(\${application.auth.access-token.age})}")
    private val accessTokenAge: Duration = Duration.ofHours(1),
    @Value("#{T(java.time.Duration).ofSeconds(\${application.auth.refresh-token.age})}")
    private val refreshTokenAge: Duration = Duration.ofDays(30)
) {
    suspend fun issue(principal: Principal): Tokens {
        val accessTokenCreateScope = scopeTokenFinder.findByNameOrFail("access-token:create", cache = true)
        val refreshTokenCreateScope = scopeTokenFinder.findByNameOrFail("refresh-token:create", cache = true)

        if (!principal.hasScope(accessTokenCreateScope)) {
            throw RequiredPermissionException()
        }

        val claim = claimEmbedder.embedding(principal)

        val accessTokenScope =
            principal.scope.filter { it.id != accessTokenCreateScope.id && it.id != refreshTokenCreateScope.id }
        val accessToken = tokenEncoder.encode(
            claim,
            accessTokenAge,
            scope = accessTokenScope
        )

        val refreshToken = if (principal.hasScope(refreshTokenCreateScope)) {
            val refreshTokenScope = principal.scope.filter { it.id != refreshTokenCreateScope.id }
            tokenEncoder.encode(
                claim,
                refreshTokenAge,
                scope = refreshTokenScope
            )
        } else null

        return Tokens(
            accessToken = accessToken,
            tokenType = "bearer",
            expiresIn = accessTokenAge,
            refreshToken = refreshToken
        )
    }
}
