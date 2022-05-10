package io.github.siyual_park.auth.domain.authentication

import org.springframework.stereotype.Component

@Component
class AllAuthenticateFilter : AuthenticateFilter {
    override fun isSubscribe(payload: Any): Boolean {
        return true
    }
}
