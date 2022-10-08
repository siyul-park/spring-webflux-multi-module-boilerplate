package io.github.siyual_park.auth.domain.authentication

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.exception.AuthorizeException
import io.github.siyual_park.auth.exception.UnauthorizatedException
import org.springframework.stereotype.Component

@Suppress("UNCHECKED_CAST")
@Component
class Authenticator {
    private val strategies = mutableListOf<Pair<AuthenticateFilter, AuthenticateStrategy<*, *>>>()

    fun register(filter: AuthenticateFilter, strategy: AuthenticateStrategy<*, *>): Authenticator {
        strategies.add(filter to strategy)
        return this
    }

    suspend fun <PAYLOAD : Any> authenticate(payload: PAYLOAD): Principal {
        val strategies = strategies
            .filter { (_, strategy) -> strategy.clazz.isInstance(payload) }
            .filter { (filter, _) -> filter.isSubscribe(payload) }
            .map { (_, strategy) -> strategy }

        var exception: AuthorizeException? = null
        for (strategy in strategies) {
            strategy as AuthenticateStrategy<PAYLOAD, *>
            try {
                val principal = strategy.authenticate(payload)
                if (principal != null) {
                    return principal
                }
            } catch (e: AuthorizeException) {
                exception = e
            }
        }

        throw UnauthorizatedException(exception?.message, exception)
    }
}
