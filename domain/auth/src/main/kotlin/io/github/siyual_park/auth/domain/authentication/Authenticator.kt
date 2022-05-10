package io.github.siyual_park.auth.domain.authentication

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.exception.UnauthorizatedException
import org.springframework.stereotype.Component

@Suppress("UNCHECKED_CAST")
@Component
class Authenticator {
    private val strategies = mutableListOf<Pair<AuthenticateFilter, AuthenticateStrategy<*, *>>>()
    private val pipelines = mutableListOf<Pair<AuthenticateFilter, AuthenticatePipeline<*>>>()

    fun register(filter: AuthenticateFilter, strategy: AuthenticateStrategy<*, *>): Authenticator {
        strategies.add(filter to strategy)
        return this
    }

    fun register(filter: AuthenticateFilter, pipeline: AuthenticatePipeline<*>): Authenticator {
        pipelines.add(filter to pipeline)
        return this
    }

    suspend fun <PAYLOAD : Any> authenticate(payload: PAYLOAD): Principal {
        val strategies = strategies
            .filter { (_, strategy) -> strategy.clazz.isInstance(payload) }
            .filter { (filter, _) -> filter.isSubscribe(payload) }
            .map { (_, strategy) -> strategy }
        val pipelines = pipelines
            .filter { (filter, _) -> filter.isSubscribe(payload) }
            .map { (_, pipeline) -> pipeline }

        var exception: RuntimeException? = null
        for (strategy in strategies) {
            strategy as AuthenticateStrategy<PAYLOAD, *>
            try {
                val principal = strategy.authenticate(payload)
                if (principal != null) {
                    return pipelines.fold(principal) { acc, pipeline ->
                        if (pipeline.clazz.isInstance(acc)) {
                            (pipeline as AuthenticatePipeline<Principal>).pipe(acc)
                        } else {
                            acc
                        }
                    }
                }
            } catch (e: RuntimeException) {
                exception = e
            }
        }

        throw UnauthorizatedException(exception?.message, exception)
    }
}
