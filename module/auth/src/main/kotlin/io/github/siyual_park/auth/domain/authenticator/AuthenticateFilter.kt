package io.github.siyual_park.auth.domain.authenticator

interface AuthenticateFilter {
    fun isSubscribe(payload: AuthenticationPayload): Boolean
}
