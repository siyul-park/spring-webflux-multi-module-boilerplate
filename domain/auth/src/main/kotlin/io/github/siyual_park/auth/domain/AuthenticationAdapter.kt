package io.github.siyual_park.auth.domain

import org.springframework.security.core.Authentication

class AuthenticationAdapter(
    private val principal: Principal,
    private val credentials: String
) : Authentication {
    private var authenticated = true
    private val authorities = principal.scope.map { GrantedAuthorityAdapter(it) }

    override fun getName(): String {
        return principal.id.toString()
    }

    override fun getAuthorities(): List<GrantedAuthorityAdapter> {
        return authorities
    }

    override fun getCredentials(): String {
        return credentials
    }

    override fun getDetails(): Principal {
        return principal
    }

    override fun getPrincipal(): Principal {
        return principal
    }

    override fun isAuthenticated(): Boolean {
        return authenticated
    }

    override fun setAuthenticated(isAuthenticated: Boolean) {
        authenticated = isAuthenticated
    }
}
