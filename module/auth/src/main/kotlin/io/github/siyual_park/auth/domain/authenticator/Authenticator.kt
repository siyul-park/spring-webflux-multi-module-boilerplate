package io.github.siyual_park.auth.domain.authenticator

import io.github.siyual_park.auth.domain.Principal

interface Authenticator<PAYLOAD : AuthenticationPayload, PRINCIPAL : Principal> {
    suspend fun authenticate(payload: PAYLOAD): PRINCIPAL
}
