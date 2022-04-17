package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.entity.TokenData
import io.github.siyual_park.auth.repository.TokenRepository
import io.github.siyual_park.util.retry
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.Random

@Component
class TokenFactory(
    private val claimEmbedder: ClaimEmbedder,
    private val tokenRepository: TokenRepository,
    private val tokenMapper: TokenMapper,
) {
    private val random = SecureRandom.getInstance("SHA1PRNG")

    init {
        random.setSeed(random.generateSeed(128))
    }

    suspend fun create(
        principal: Principal,
        age: Duration,
        pop: Set<ScopeToken>? = null,
        push: Set<ScopeToken>? = null,
        filter: Set<ScopeToken>? = null
    ): Token {
        val baseClaims = claimEmbedder.embedding(principal)
        val scope = mutableSetOf<ScopeToken>().also { scope ->
            scope.addAll(
                principal.scope
                    .filter { filter?.contains(it) ?: true }
                    .filter { pop?.contains(it)?.let { !it } ?: true }
            )
            push?.let { scope.addAll(it) }
        }

        val claims = mutableMapOf<String, Any>()
        claims.putAll(baseClaims)
        claims["scope"] = scope.map { it.id.toString() }

        val now = Instant.now()
        val expiredAt = now.plus(age)
        val data = retry(3) {
            TokenData(
                signature = generateSignature(64),
                claims = claims,
                expiredAt = expiredAt
            ).let { tokenRepository.create(it) }
        }

        return tokenMapper.map(data)
    }

    @Suppress("SameParameterValue")
    private fun generateSignature(length: Int): String {
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        val stringBuilder = StringBuilder(length)
        for (i in 0 until length) {
            stringBuilder.append(chars[random.nextInt(chars.length)])
        }
        return stringBuilder.toString()
    }
}
