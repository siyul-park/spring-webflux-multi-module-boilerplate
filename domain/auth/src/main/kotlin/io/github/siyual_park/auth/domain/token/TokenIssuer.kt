package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class TokenIssuer(
    private val tokenParser: TokenParser,
    private val claimEmbedder: ClaimEmbedder
) {
    suspend fun issue(
        principal: Principal,
        age: Duration,
        pop: Set<ScopeToken>? = null,
        push: Set<ScopeToken>? = null,
        filter: Set<ScopeToken>? = null
    ): Token {
        val claim = claimEmbedder.embedding(principal)
        val scope = mutableSetOf<ScopeToken>().also { scope ->
            scope.addAll(
                principal.scope
                    .filter { filter?.contains(it) ?: true }
                    .filter { pop?.contains(it)?.let { !it } ?: true }
            )
            push?.let { scope.addAll(it) }
        }

        val token = tokenParser.encode(claim, age, scope = scope)

        return Token(
            value = token,
            type = "bearer",
            expiresIn = age,
        )
    }
}
