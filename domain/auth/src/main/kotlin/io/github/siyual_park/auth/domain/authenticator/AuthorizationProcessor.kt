package io.github.siyual_park.auth.domain.authenticator

import io.github.siyual_park.auth.domain.Principal

interface AuthorizationProcessor<PRINCIPAL : Principal> {
    val type: String

    suspend fun authenticate(credentials: String): PRINCIPAL?
}
