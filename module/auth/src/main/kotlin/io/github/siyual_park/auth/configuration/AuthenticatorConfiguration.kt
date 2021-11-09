package io.github.siyual_park.auth.configuration

import io.github.siyual_park.auth.domain.authenticator.AuthenticateFilterFactory
import io.github.siyual_park.auth.domain.authenticator.AuthenticateMapping
import io.github.siyual_park.auth.domain.authenticator.AuthenticateProcessor
import io.github.siyual_park.auth.domain.authenticator.Authenticator
import io.github.siyual_park.auth.domain.authenticator.AuthorizationAuthenticateProcessor
import io.github.siyual_park.auth.domain.authenticator.AuthorizationProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration

@Configuration
class AuthenticatorConfiguration(
    private val applicationContext: ApplicationContext
) {
    @Autowired(required = true)
    fun configAuthenticatorManager(authenticator: Authenticator, filterFactory: AuthenticateFilterFactory) {
        applicationContext.getBeansOfType(AuthenticateProcessor::class.java).values.forEach {
            it.javaClass.annotations.filter { it is AuthenticateMapping }
                .forEach { annotation ->
                    if (annotation !is AuthenticateMapping) return@forEach
                    val filter = filterFactory.create(annotation) ?: return@forEach
                    authenticator.register(filter, it)
                }
        }
    }

    @Autowired(required = true)
    fun configAuthorizationAuthenticator(authorizationAuthenticator: AuthorizationAuthenticateProcessor) {
        applicationContext.getBeansOfType(AuthorizationProcessor::class.java).values.forEach {
            authorizationAuthenticator.register(it)
        }
    }
}
