package io.github.siyual_park.auth.domain.token

import org.springframework.stereotype.Component

@Suppress("UNCHECKED_CAST")
@Component
class ClaimEmbedder {
    private val processors = mutableListOf<Pair<ClaimEmbedFilter, ClaimEmbeddingStrategy<*>>>()

    fun <T : Any> register(filter: ClaimEmbedFilter, processor: ClaimEmbeddingStrategy<T>): ClaimEmbedder {
        processors.add(filter to processor)
        return this
    }

    suspend fun <PRINCIPAL : Any> embedding(principal: PRINCIPAL): Map<String, Any> {
        val processors = processors
            .filter { (_, strategy) -> strategy.clazz.isInstance(principal) }
            .filter { (filter, _) -> filter.isSubscribe(principal) }
            .map { (_, strategy) -> strategy }

        return processors.map {
            it as ClaimEmbeddingStrategy<PRINCIPAL>
            it.embedding(principal)
        }.fold(mutableMapOf()) { acc, cur -> acc.also { it.putAll(cur) } }
    }
}
