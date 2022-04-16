package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.ulid.ULID
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toSet
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Suppress("UNCHECKED_CAST")
@Component
class TokenParser(
    @Value("\${application.auth.secret}") private val secret: String,
    private val scopeTokenStorage: ScopeTokenStorage
) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun encode(
        claims: Claims,
        age: Duration,
        scope: Iterable<ScopeToken>? = null
    ): String {
        val now = Instant.now()
        val filteredScope = scope?.filter { claims.scope.contains(it) } ?: claims.scope

        return Jwts.builder().apply {
            claim("jti", UUID.randomUUID().toString())
            claim("scope", filteredScope.mapNotNull { it.id }.joinToString(" "))
            setIssuedAt(Date.from(now))
            setExpiration(Date.from(now.plus(age)))
            signWith(secretKey, SignatureAlgorithm.HS256)

            claims.filter { (key, _) -> key != "scope" }
                .forEach { (key, value) ->
                    claim(key, value)
                }
        }
            .compact()
    }

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
        return scope.split(" ")
            .map { ULID.fromString(it) }
            .let { scopeTokenStorage.load(it) }
    }
}
