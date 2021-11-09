package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.Principal
import kotlin.reflect.KClass

interface ClaimEmbeddingProcessor<PRINCIPAL : Principal> {
    val principalClazz: KClass<PRINCIPAL>

    suspend fun embedding(principal: PRINCIPAL): Claims
}
