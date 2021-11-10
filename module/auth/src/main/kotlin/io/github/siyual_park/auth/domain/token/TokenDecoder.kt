package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFinder
import io.github.siyual_park.auth.entity.ScopeToken
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toSet
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.crypto.SecretKey

@Component
class TokenDecoder(
    @Value("\${application.auth.secret}") private val secret: String,
    private val scopeTokenFinder: ScopeTokenFinder
) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    suspend fun decode(token: String): Claims {
        val jwt = Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parse(token)
        val body = jwt.body as Map<String, Any>

        val claims = Claims()
        body.forEach { (key, value) ->
            if (key != "scope") {
                claims[key] = value
            } else {
                val scope = value.toString()
                val decodedScope = decodeScope(scope.trim()).toSet()
                claims.scope = decodedScope
            }
        }

        return claims
    }

    private fun decodeScope(scope: String): Flow<ScopeToken> {
        return scopeTokenFinder.findAllById(
            scope.split(" ").mapNotNull { it.toLongOrNull() },
        )
    }
}
