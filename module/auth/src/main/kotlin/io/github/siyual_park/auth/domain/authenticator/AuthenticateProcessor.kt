package io.github.siyual_park.auth.domain.authenticator

import io.github.siyual_park.auth.domain.Principal

interface AuthenticateProcessor<PAYLOAD : Any, PRINCIPAL : Principal> {
    suspend fun authenticate(payload: PAYLOAD): PRINCIPAL
}
