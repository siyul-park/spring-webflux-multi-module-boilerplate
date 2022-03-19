package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.Principal

interface ClaimEmbeddingProcessor<PRINCIPAL : Principal> {
    suspend fun embedding(principal: PRINCIPAL): Claims
}
