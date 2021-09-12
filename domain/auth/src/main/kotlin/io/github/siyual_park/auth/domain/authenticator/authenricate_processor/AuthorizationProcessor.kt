package io.github.siyual_park.auth.domain.authenticator.authenricate_processor

import io.github.siyual_park.auth.domain.principal.Principal

interface AuthorizationProcessor<PRINCIPAL : Principal> {
    val type: String

    suspend fun authenticate(credentials: String): PRINCIPAL
}
