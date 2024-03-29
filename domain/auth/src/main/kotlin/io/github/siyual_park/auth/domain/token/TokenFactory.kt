package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.entity.TokenEntity
import io.github.siyual_park.auth.repository.TokenEntityRepository
import io.github.siyual_park.data.criteria.and
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.data.patch.SuspendPatch
import io.github.siyual_park.util.retry
import kotlinx.coroutines.flow.toList
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant

class TokenFactory(
    private val template: TokenTemplate,
    private val claimEmbedder: ClaimEmbedder,
    private val tokenEntityRepository: TokenEntityRepository,
    private val tokenMapper: TokenMapper,
) {
    private val random = SecureRandom.getInstance("SHA1PRNG").apply {
        setSeed(generateSeed(128))
    }

    suspend fun create(
        principal: Principal,
        claims: Map<String, Any>? = null,
        pop: Set<ScopeToken>? = null,
        push: Set<ScopeToken>? = null,
        filter: Set<ScopeToken>? = null,
    ): Token {
        val finalPop = merge(template.pop, pop)
        val finalPush = merge(template.push, push)
        val finalFilter = merge(template.filter, filter)

        val baseClaims = claimEmbedder.embedding(principal).mapValues { (_, value) ->
            if (value is String) {
                value
            } else {
                value.toString()
            }
        }
        val scope = mutableSetOf<ScopeToken>().also { scope ->
            scope.addAll(
                principal.scope
                    .filter { finalFilter?.contains(it) ?: true }
                    .filter { finalPop?.contains(it)?.let { !it } ?: true }
            )
            finalPush?.let { scope.addAll(it) }
        }

        val finalClaims = claims?.toMutableMap() ?: mutableMapOf()
        finalClaims.putAll(baseClaims)
        finalClaims["scope"] = scope.map { it.id.toString() }
        finalClaims["type"] = template.type

        deprecated(finalClaims)

        val now = Instant.now()
        val expiredAt = now.plus(template.age)
        val data = retry(3) {
            TokenEntity(
                type = template.type,
                signature = generateSignature(template.type),
                claims = finalClaims,
                expiredAt = expiredAt
            ).let { tokenEntityRepository.create(it) }
        }

        return tokenMapper.map(data)
    }

    private fun merge(x: Set<ScopeToken>?, y: Set<ScopeToken>?): Set<ScopeToken>? {
        if (x == null && y == null) {
            return null
        }

        return mutableSetOf<ScopeToken>()
            .apply {
                x?.let { addAll(it) }
                y?.let { addAll(it) }
            }
    }

    private suspend fun deprecated(claims: Map<String, Any>) {
        val now = Instant.now()
        val expiredAt = now.plus(Duration.ofMinutes(1))

        template.limit?.forEach { (key, limit) ->
            val value = claims[key] ?: return@forEach
            val query = where("claims.$key").`is`(value)
                .and(where(TokenEntity::type).`is`(template.type))
                .and(where(TokenEntity::expiredAt).greaterThan(expiredAt))

            val count = tokenEntityRepository.count(query, limit = limit)

            tokenEntityRepository.updateAll(
                query,
                SuspendPatch.with {
                    it.expiredAt = expiredAt
                },
                limit = (count - limit + 1).toInt()
            ).toList()
        }
    }

    private fun generateSignature(type: String?): String {
        val length = 40
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        val stringBuilder = StringBuilder(length)
        for (i in 0 until length) {
            stringBuilder.append(chars[random.nextInt(chars.length)])
        }

        if (type == null) {
            return stringBuilder.toString()
        }

        return "${type}_$stringBuilder"
    }
}
