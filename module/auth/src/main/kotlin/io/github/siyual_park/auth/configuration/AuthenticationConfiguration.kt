package io.github.siyual_park.auth.configuration

import io.github.siyual_park.auth.domain.authentication.AuthenticateFilterFactory
import io.github.siyual_park.auth.domain.authentication.AuthenticateMapping
import io.github.siyual_park.auth.domain.authentication.AuthenticateProcessor
import io.github.siyual_park.auth.domain.authentication.Authenticator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration

@Configuration
class AuthenticationConfiguration(
    private val applicationContext: ApplicationContext
) {
    @Autowired(required = true)
    fun configAuthenticator(authenticator: Authenticator, filterFactory: AuthenticateFilterFactory) {
        applicationContext.getBeansOfType(AuthenticateProcessor::class.java).values.forEach {
            it.javaClass.annotations.filter { it is AuthenticateMapping }
                .forEach { annotation ->
                    if (annotation !is AuthenticateMapping) return@forEach
                    val filter = filterFactory.create(annotation) ?: return@forEach
                    authenticator.register(filter, it)
                }
        }
    }
}
