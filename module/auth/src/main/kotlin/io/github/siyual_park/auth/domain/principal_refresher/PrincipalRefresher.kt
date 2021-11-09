package io.github.siyual_park.auth.domain.principal_refresher

import io.github.siyual_park.auth.domain.Principal
import org.springframework.stereotype.Component

@Suppress("UNCHECKED_CAST")
@Component
class PrincipalRefresher {
    private val processors = mutableMapOf<Class<*>, PrincipalRefreshProcessor<*>>()

    fun <T : Principal> register(processor: PrincipalRefreshProcessor<T>) {
        processors[processor.principalClazz.java] = processor
    }

    suspend fun <T : Principal> refresh(principal: T): T {
        val processor = processors[principal.javaClass] ?: return principal
        processor as PrincipalRefreshProcessor<T>
        return processor.refresh(principal)
    }
}
