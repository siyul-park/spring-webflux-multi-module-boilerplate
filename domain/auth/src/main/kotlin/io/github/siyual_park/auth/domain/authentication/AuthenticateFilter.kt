package io.github.siyual_park.auth.domain.authentication

interface AuthenticateFilter {
    fun isSubscribe(payload: Any): Boolean
}
