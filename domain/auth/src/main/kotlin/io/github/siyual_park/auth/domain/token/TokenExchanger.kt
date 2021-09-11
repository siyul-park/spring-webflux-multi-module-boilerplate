package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.authenticator.Principal
import io.github.siyual_park.auth.domain.authenticator.UserPrincipal
import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.auth.entity.ids
import io.github.siyual_park.auth.exception.PrincipalIdNotExistsException
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
    @Value("\${application.auth.secret}") private val secret: String
) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun encoding(
        principal: Principal,
        age: Duration,
        scope: Iterable<ScopeToken>? = null
    ): String {
        val now = Instant.now()
        val filteredScope = scope?.filter { principal.scope.contains(it) } ?: principal.scope

        return Jwts.builder().apply {
            claim("jti", UUID.randomUUID().toString())
            claim("scope", filteredScope.ids().joinToString(" "))
            setIssuedAt(Date.from(now))
            setExpiration(Date.from(now.plus(age)))
            signWith(secretKey, SignatureAlgorithm.HS256)

            if (principal is UserPrincipal) {
                claim("uid", principal.id)
            }
        }
            .compact()
    }

    @Cacheable("TokenManager.decode(String)")
    fun decode(token: String): Principal {
        val jwt = Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parse(token)

        val body = jwt.body as Claims
        val scope = body["scope"].toString()
        val uid = body["uid"] ?: throw PrincipalIdNotExistsException()

        val scopeTokens = decodeScope(scope).toSet()

        return object : Principal {
            override val id: String
                get() = uid.toString()
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
