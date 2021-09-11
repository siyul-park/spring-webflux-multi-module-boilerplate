package io.github.siyual_park.auth.domain.authenticator

import io.github.siyual_park.auth.exception.UnsupportedAuthorizationTypeException
import org.springframework.stereotype.Component

@Component
class AuthorizationAuthenticator : Authenticator<AuthorizationPayload, Principal> {
    override val payloadClazz = AuthorizationPayload::class

    private val processors = mutableMapOf<String, AuthorizationAuthenticateProcessor<*>>()

    fun <PRINCIPAL : Principal> register(
        processor: AuthorizationAuthenticateProcessor<PRINCIPAL>
    ): AuthorizationAuthenticator {
        processors[processor.type.lowercase()] = processor
        return this
    }

    override suspend fun authenticate(payload: AuthorizationPayload): Principal {
        val processor = processors[payload.type.lowercase()] ?: throw UnsupportedAuthorizationTypeException()
        return processor.authenticate(payload.credentials)
    }
}
