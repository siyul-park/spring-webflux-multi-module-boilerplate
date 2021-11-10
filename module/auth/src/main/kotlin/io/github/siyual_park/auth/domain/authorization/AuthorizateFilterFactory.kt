package io.github.siyual_park.auth.domain.authorization

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class AuthorizateFilterFactory(
    private val applicationContext: ApplicationContext
) {
    fun create(mapping: AuthorizeMapping): AuthorizeFilter? {
        val filterBeen = try {
            applicationContext.getBean(mapping.filterBy.java)
        } catch (e: BeansException) {
            null
        }

        return if (filterBeen is AuthorizeFilter) {
            filterBeen
        } else {
            null
        }
    }
}
