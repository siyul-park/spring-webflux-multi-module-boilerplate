package io.github.siyual_park.auth.domain.authenticator

import kotlin.reflect.KClass

interface Authenticator<PAYLOAD : AuthenticationPayload, AUTHENTICATION : Authentication<ID>, ID> {
    val payloadClazz: KClass<PAYLOAD>

    suspend fun authenticate(payload: PAYLOAD): AUTHENTICATION
}
