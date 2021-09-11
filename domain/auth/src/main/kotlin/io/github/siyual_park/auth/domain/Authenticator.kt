package io.github.siyual_park.auth.domain

interface Authenticator<INFO : AuthenticationInformation, AUTHENTICATION : Authentication<ID>, ID> {
    fun authenticate(info: INFO): AUTHENTICATION
}
