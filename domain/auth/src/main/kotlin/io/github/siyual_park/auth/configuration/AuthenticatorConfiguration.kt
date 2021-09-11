package io.github.siyual_park.auth.configuration

import io.github.siyual_park.auth.domain.authenticator.Authenticator
import io.github.siyual_park.auth.domain.authenticator.AuthenticatorManager
import io.github.siyual_park.auth.domain.authenticator.AuthorizationAuthenticateProcessor
import io.github.siyual_park.auth.domain.authenticator.AuthorizationAuthenticator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration

@Configuration
class AuthenticatorConfiguration(
    private val applicationContext: ApplicationContext
) {
    @Autowired(required = true)
    fun configAuthenticatorManager(authenticatorManager: AuthenticatorManager) {
        val mappers = applicationContext.getBeansOfType(Authenticator::class.java)
        mappers.values.forEach {
            authenticatorManager.register(it)
        }
    }

    @Autowired(required = true)
    fun configAuthorizationAuthenticator(authorizationAuthenticator: AuthorizationAuthenticator) {
        val mappers = applicationContext.getBeansOfType(AuthorizationAuthenticateProcessor::class.java)
        mappers.values.forEach {
            authorizationAuthenticator.register(it)
        }
    }
}
