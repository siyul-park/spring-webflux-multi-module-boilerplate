package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.entity.ScopeToken
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.crypto.SecretKey

@Component
class TokenDecoder(
    @Value("\${application.auth.secret}") private val secret: String
) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun decode(token: String): Claims {
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

    private fun decodeScope(scope: String): List<ScopeToken> {
        if (scope.isEmpty()) {
            return emptyList()
        }

        val scopeTokens = scope.split(" ")
        return scopeTokens.map {
            ScopeToken(
                name = "",
                description = null,
                system = true,
            ).apply {
                id = it.toLong()
            }
        }
    }
}
