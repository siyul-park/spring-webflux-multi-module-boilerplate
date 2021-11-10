package io.github.siyual_park.auth.domain.authentication

object EmptyAuthenticateFilter : AuthenticateFilter {
    override fun isSubscribe(payload: Any): Boolean {
        return false
    }
}
