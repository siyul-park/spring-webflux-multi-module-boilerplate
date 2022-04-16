package io.github.siyual_park.auth.domain.authentication

import io.github.siyual_park.auth.domain.Principal

interface AuthenticateStrategy<PAYLOAD : Any, PRINCIPAL : Principal> {
    suspend fun authenticate(payload: PAYLOAD): PRINCIPAL?
}
