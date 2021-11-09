package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.exception.UnsupportedPrincipalException
import org.springframework.stereotype.Component

@Suppress("UNCHECKED_CAST")
@Component
class ClaimEmbedder {
    private val processors = mutableMapOf<Class<*>, ClaimEmbeddingProcessor<*>>()

    fun <T : Principal> register(processor: ClaimEmbeddingProcessor<T>): ClaimEmbedder {
        processors[processor.principalClazz.java] = processor
        return this
    }

    suspend fun <T : Principal> embedding(principal: T): Claims {
        val processor = processors[principal.javaClass] ?: throw UnsupportedPrincipalException()
        processor as ClaimEmbeddingProcessor<T>
        return processor.embedding(principal)
    }
}
