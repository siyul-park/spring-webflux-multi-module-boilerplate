package io.github.siyual_park.auth.domain.authenticator

interface Authenticator<INFO : AuthenticationInfo, AUTHENTICATION : Authentication<ID>, ID> {
    suspend fun authenticate(info: INFO): AUTHENTICATION
}
