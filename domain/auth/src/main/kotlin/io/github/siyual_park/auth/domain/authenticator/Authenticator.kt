package io.github.siyual_park.auth.domain.authenticator

import io.github.siyual_park.auth.domain.authenticator.payload.AuthenticationPayload
import io.github.siyual_park.auth.domain.principal.Principal
import kotlin.reflect.KClass

interface Authenticator<PAYLOAD : AuthenticationPayload, PRINCIPAL : Principal> {
    val payloadClazz: KClass<PAYLOAD>

    suspend fun authenticate(payload: PAYLOAD): PRINCIPAL
}
