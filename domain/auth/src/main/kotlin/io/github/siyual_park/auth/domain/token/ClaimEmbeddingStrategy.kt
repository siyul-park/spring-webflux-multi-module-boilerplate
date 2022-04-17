package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.Principal

interface ClaimEmbeddingStrategy<PRINCIPAL : Principal> {
    suspend fun embedding(principal: PRINCIPAL): Map<String, Any>
}
