package io.github.siyual_park.auth.domain.authentication

class MatchTypeAuthenticateFilter<T>(
    private val clazz: Class<T>
) : AuthenticateFilter {
    override fun isSubscribe(payload: Any): Boolean {
        return payload.javaClass == clazz
    }
}
