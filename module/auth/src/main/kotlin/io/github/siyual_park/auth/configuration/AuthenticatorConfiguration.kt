package io.github.siyual_park.auth.configuration

import io.github.siyual_park.auth.annotation.AuthenticateFilter
import io.github.siyual_park.auth.domain.authenticator.Authenticator
import io.github.siyual_park.auth.domain.authenticator.AuthenticatorManager
import io.github.siyual_park.auth.domain.authenticator.AuthorizationAuthenticator
import io.github.siyual_park.auth.domain.authenticator.AuthorizationProcessor
import io.github.siyual_park.auth.domain.authenticator.MatchTypeAuthenticateFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration

@Configuration
class AuthenticatorConfiguration(
    private val applicationContext: ApplicationContext
) {
    @Autowired(required = true)
    fun configAuthenticatorManager(authenticatorManager: AuthenticatorManager) {
        applicationContext.getBeansOfType(Authenticator::class.java).values.forEach {
            it.javaClass.annotations.filter { it is AuthenticateFilter }
                .forEach { annotation ->
                    val filter = annotation as? AuthenticateFilter ?: return@forEach
                    authenticatorManager.register(
                        MatchTypeAuthenticateFilter(filter.type),
                        it
                    )
                }
        }
    }

    @Autowired(required = true)
    fun configAuthorizationAuthenticator(authorizationAuthenticator: AuthorizationAuthenticator) {
        applicationContext.getBeansOfType(AuthorizationProcessor::class.java).values.forEach {
            authorizationAuthenticator.register(it)
        }
    }
}
