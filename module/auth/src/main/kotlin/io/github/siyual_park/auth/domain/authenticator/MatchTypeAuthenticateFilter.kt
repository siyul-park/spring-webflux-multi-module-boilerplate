package io.github.siyual_park.auth.domain.authenticator

import kotlin.reflect.KClass

class MatchTypeAuthenticateFilter<T : Any>(
    private val clazz: KClass<T>
) : AuthenticateFilter {
    override fun isSubscribe(payload: Any): Boolean {
        return payload.javaClass == clazz.java
    }
}
