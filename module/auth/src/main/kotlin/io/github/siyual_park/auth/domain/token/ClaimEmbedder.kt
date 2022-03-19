package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.exception.UnsupportedPrincipalException
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
@Component
class ClaimEmbedder {
    private val processors = mutableMapOf<Class<*>, ClaimEmbeddingProcessor<*>>()

    fun <T : Principal> register(clazz: KClass<T>, processor: ClaimEmbeddingProcessor<T>): ClaimEmbedder {
        processors[clazz.java] = processor
        return this
    }

    suspend fun <T : Principal> embedding(principal: T): Claims {
        val processor = processors[principal.javaClass] ?: throw UnsupportedPrincipalException()
        processor as ClaimEmbeddingProcessor<T>
        return processor.embedding(principal)
    }
}
