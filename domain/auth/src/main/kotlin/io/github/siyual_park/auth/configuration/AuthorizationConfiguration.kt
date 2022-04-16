package io.github.siyual_park.auth.configuration

import io.github.siyual_park.auth.domain.authorization.Authorizator
import io.github.siyual_park.auth.domain.authorization.AuthorizeFilter
import io.github.siyual_park.auth.domain.authorization.AuthorizeMapping
import io.github.siyual_park.auth.domain.authorization.AuthorizeStrategy
import io.github.siyual_park.auth.domain.authorization.ScopeMapping
import io.github.siyual_park.auth.domain.authorization.ScopeMatchAuthorizeFilter
import org.springframework.beans.BeansException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration

@Configuration
class AuthorizationConfiguration(
    private val applicationContext: ApplicationContext
) {
    @Autowired(required = true)
    fun configAuthorizator(authorizator: Authorizator) {
        applicationContext.getBeansOfType(AuthorizeStrategy::class.java).values.forEach {
            val filter = getFilter(it) ?: return@forEach
            authorizator.register(filter, it)
        }
    }

    private fun getFilter(strategy: AuthorizeStrategy): AuthorizeFilter? {
        val authMapping = strategy.javaClass.annotations.filterIsInstance<AuthorizeMapping>().firstOrNull() ?: return null
        if (authMapping.filterBy == ScopeMatchAuthorizeFilter::class) {
            val scopeMapping = strategy.javaClass.annotations.filterIsInstance<ScopeMapping>().firstOrNull() ?: return null
            return ScopeMatchAuthorizeFilter(
                scopeTokens = scopeMapping.scope.toSet()
            )
        }

        val filterBeen = try {
            applicationContext.getBean(authMapping.filterBy.java)
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
