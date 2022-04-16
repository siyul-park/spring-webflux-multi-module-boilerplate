package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.exception.UnsupportedPrincipalException
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
@Component
class ClaimEmbedder {
    private val processors = mutableMapOf<Class<*>, ClaimEmbeddingStrategy<*>>()

    fun <T : Principal> register(clazz: KClass<T>, processor: ClaimEmbeddingStrategy<T>): ClaimEmbedder {
        processors[clazz.java] = processor
        return this
    }

    suspend fun <T : Principal> embedding(principal: T): Claims {
        val processor = processors[principal.javaClass] ?: throw UnsupportedPrincipalException()
        processor as ClaimEmbeddingStrategy<T>
        return processor.embedding(principal)
    }
}
