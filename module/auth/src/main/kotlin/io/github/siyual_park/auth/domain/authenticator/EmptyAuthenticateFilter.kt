package io.github.siyual_park.auth.domain.authenticator

object EmptyAuthenticateFilter : AuthenticateFilter {
    override fun isSubscribe(payload: Any): Boolean {
        return false
    }
}
