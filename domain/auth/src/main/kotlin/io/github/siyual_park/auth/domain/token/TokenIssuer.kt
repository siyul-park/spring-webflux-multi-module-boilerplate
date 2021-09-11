package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.authenticator.Principal
import io.github.siyual_park.auth.exception.RequiredPermissionException
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class TokenIssuer(
    private val scopeTokenRepository: ScopeTokenRepository,
    private val tokenExchanger: TokenExchanger,
    @Value("#{T(java.time.Duration).ofSeconds(\${application.auth.access-token.age})}")
    private val accessTokenAge: Duration = Duration.ofHours(1),
    @Value("#{T(java.time.Duration).ofSeconds(\${application.auth.refresh-token.age})}")
    private val refreshTokenAge: Duration = Duration.ofDays(30)
) {
    suspend fun issue(principal: Principal): Tokens {
        val accessTokenCreateScope = scopeTokenRepository.findByNameOrFail("access-token:create")
        val refreshTokenCreateScope = scopeTokenRepository.findByNameOrFail("refresh-token:create")

        if (!principal.scope.contains(accessTokenCreateScope)) {
            throw RequiredPermissionException()
        }

        val accessToken = tokenExchanger.encoding(
            principal,
            accessTokenAge,
            scope = principal.scope.filter { it.id != accessTokenCreateScope.id && it.id != refreshTokenCreateScope.id }
        )
        val refreshToken = if (principal.scope.contains(refreshTokenCreateScope)) {
            tokenExchanger.encoding(
                principal,
                refreshTokenAge,
                scope = listOf(accessTokenCreateScope)
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
