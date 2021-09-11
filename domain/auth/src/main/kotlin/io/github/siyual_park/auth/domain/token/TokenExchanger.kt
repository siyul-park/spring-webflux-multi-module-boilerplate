package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.authenticator.Principal
import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.auth.entity.ids
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class TokenExchanger(
    @Value("\${application.secret}") private val secret: String
) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun encoding(principal: Principal<*>, age: Duration): String {
        val now = Instant.now()

        return Jwts.builder()
            .claim("jti", UUID.randomUUID().toString())
            .claim("sub", principal.id)
            .claim("scope", principal.scope.ids().joinToString(" "))
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plus(age)))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }

    @Cacheable("TokenManager.decode(String)")
    fun decode(token: String): Principal<String> {
        val jwt = Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parse(token)

        val body = jwt.body as Claims
        val scope = body["scope"] as String

        val scopeTokens = decodeScope(scope).toSet()

        return object : Principal<String> {
            override val id: String
                get() = body["jti"] as String
            override val scope: Set<ScopeToken>
                get() = scopeTokens
        }
    }

    fun decodeScope(scope: String): List<ScopeToken> {
        val scopeTokens = scope.split(" ")
        return scopeTokens.map {
            ScopeToken(
                name = "",
                description = null,
                system = true,
                default = false
            ).apply {
                id = it.toLong()
            }
        }
    }
}
