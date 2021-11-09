package io.github.siyual_park.auth.domain.authenticator

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.exception.UnsupportedAuthorizationTypeException
import org.springframework.stereotype.Component

@Component
@AuthenticateMapping(filterBy = AuthorizationPayload::class)
class AuthorizationAuthenticateProcessor : AuthenticateProcessor<AuthorizationPayload, Principal> {
    private val processors = mutableMapOf<String, MutableList<AuthorizationProcessor<*>>>()

    fun <PRINCIPAL : Principal> register(
        processor: AuthorizationProcessor<PRINCIPAL>
    ): AuthorizationAuthenticateProcessor {
        val processors = processors.getOrPut(processor.type.lowercase()) { mutableListOf() }
        processors.add(processor)
        return this
    }

    override suspend fun authenticate(payload: AuthorizationPayload): Principal {
        val processors = processors[payload.type.lowercase()] ?: emptyList()
        for (processor in processors) {
            val principal = processor.authenticate(payload.credentials)
            if (principal != null) {
                return principal
            }
        }

        throw UnsupportedAuthorizationTypeException()
    }
}
