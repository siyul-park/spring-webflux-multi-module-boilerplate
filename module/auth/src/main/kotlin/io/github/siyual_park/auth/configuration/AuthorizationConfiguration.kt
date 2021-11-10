package io.github.siyual_park.auth.configuration

import io.github.siyual_park.auth.domain.authorization.AuthorizateFilterFactory
import io.github.siyual_park.auth.domain.authorization.Authorizator
import io.github.siyual_park.auth.domain.authorization.AuthorizeMapping
import io.github.siyual_park.auth.domain.authorization.AuthorizeProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration

@Configuration
class AuthorizationConfiguration(
    private val applicationContext: ApplicationContext
) {
    @Autowired(required = true)
    fun configAuthorizator(authorizator: Authorizator, filterFactory: AuthorizateFilterFactory) {
        applicationContext.getBeansOfType(AuthorizeProcessor::class.java).values.forEach {
            it.javaClass.annotations.filter { it is AuthorizeMapping }
                .forEach { annotation ->
                    if (annotation !is AuthorizeMapping) return@forEach
                    val filter = filterFactory.create(annotation) ?: return@forEach
                    authorizator.register(filter, it)
                }
        }
    }
}
