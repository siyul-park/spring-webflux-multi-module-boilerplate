package io.github.siyual_park.auth.domain.token

import kotlin.reflect.KClass

interface ClaimEmbeddingStrategy<PRINCIPAL : Any> {
    val clazz: KClass<PRINCIPAL>

    suspend fun embedding(principal: PRINCIPAL): Map<String, Any>
}
