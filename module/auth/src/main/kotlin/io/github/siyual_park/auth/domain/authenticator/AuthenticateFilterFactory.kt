package io.github.siyual_park.auth.domain.authenticator

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class AuthenticateFilterFactory(
    private val applicationContext: ApplicationContext
) {
    fun create(mapping: AuthenticateMapping): AuthenticateFilter? {
        val filterBeen = try {
            applicationContext.getBean(mapping.filterBy.java)
        } catch (e: BeansException) {
            null
        }
        return when (filterBeen) {
            null -> {
                MatchTypeAuthenticateFilter(mapping.filterBy.java)
            }
            is AuthenticateFilter -> {
                filterBeen
            }
            else -> {
                MatchTypeAuthenticateFilter(mapping.filterBy.java)
            }
        }
    }
}
