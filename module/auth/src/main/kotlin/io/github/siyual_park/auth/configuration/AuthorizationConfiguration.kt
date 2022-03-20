package io.github.siyual_park.auth.configuration

import io.github.siyual_park.auth.domain.authorization.Authorizator
import io.github.siyual_park.auth.domain.authorization.AuthorizeFilter
import io.github.siyual_park.auth.domain.authorization.AuthorizeMapping
import io.github.siyual_park.auth.domain.authorization.AuthorizeProcessor
import io.github.siyual_park.auth.domain.authorization.PrincipalMathAuthorizeFilter
import io.github.siyual_park.auth.domain.authorization.ScopeMapping
import io.github.siyual_park.auth.domain.authorization.ScopeMatchAuthorizeFilter
import io.github.siyual_park.auth.domain.authorization.then
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
        applicationContext.getBeansOfType(AuthorizeProcessor::class.java).values.forEach {
            val filter = getFilter(it) ?: return@forEach
            authorizator.register(filter, it)
        }
    }

    private fun getFilter(processor: AuthorizeProcessor<*>): AuthorizeFilter? {
        val authMapping = processor.javaClass.annotations.filterIsInstance<AuthorizeMapping>().firstOrNull() ?: return null
        val principalMatchFilter = PrincipalMathAuthorizeFilter(authMapping.principal)

        if (authMapping.filterBy == ScopeMatchAuthorizeFilter::class) {
            val scopeMapping = processor.javaClass.annotations.filterIsInstance<ScopeMapping>().firstOrNull() ?: return null
            return principalMatchFilter.then(
                ScopeMatchAuthorizeFilter(
                    scopeTokens = scopeMapping.scope.split(" ").toSet()
                )
            )
        }

        val filterBeen = try {
            applicationContext.getBean(authMapping.filterBy.java)
        } catch (e: BeansException) {
            null
        }

        return if (filterBeen is AuthorizeFilter) {
            principalMatchFilter.then(filterBeen)
        } else {
            null
        }
    }
}
