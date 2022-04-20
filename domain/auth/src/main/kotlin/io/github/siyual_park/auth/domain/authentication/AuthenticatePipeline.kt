package io.github.siyual_park.auth.domain.authentication

import io.github.siyual_park.auth.domain.Principal

interface AuthenticatePipeline<PRINCIPAL : Principal> {
    suspend fun pipe(principal: PRINCIPAL): PRINCIPAL
}
