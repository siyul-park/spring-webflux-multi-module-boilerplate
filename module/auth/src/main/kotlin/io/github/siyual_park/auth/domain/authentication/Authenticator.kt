package io.github.siyual_park.auth.domain.authentication

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.exception.UnsupportedAuthorizationTypeException
import org.springframework.stereotype.Component

@Suppress("UNCHECKED_CAST")
@Component
class Authenticator {
    private val authenticators = mutableListOf<Pair<AuthenticateFilter, AuthenticateProcessor<*, *>>>()

    fun register(filter: AuthenticateFilter, processor: AuthenticateProcessor<*, *>): Authenticator {
        authenticators.add(filter to processor)
        return this
    }

    suspend fun <PAYLOAD : Any> authenticate(payload: PAYLOAD): Principal {
        val processors = authenticators
            .filter { (filter, _) -> filter.isSubscribe(payload) }
            .map { (_, processor) -> processor }

        var exception: RuntimeException? = null
        for (processor in processors) {
            processor as AuthenticateProcessor<PAYLOAD, *>
            try {
                val principal = processor.authenticate(payload)
                if (principal != null) {
                    return principal
                }
            } catch (e: RuntimeException) {
                exception = e
            }
        }

        throw UnsupportedAuthorizationTypeException(exception?.message)
    }
}
