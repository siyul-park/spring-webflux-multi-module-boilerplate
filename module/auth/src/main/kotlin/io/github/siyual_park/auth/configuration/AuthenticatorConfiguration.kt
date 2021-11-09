package io.github.siyual_park.auth.configuration

import io.github.siyual_park.auth.annotation.AuthenticateMapping
import io.github.siyual_park.auth.domain.authenticator.AuthenticateFilter
import io.github.siyual_park.auth.domain.authenticator.Authenticator
import io.github.siyual_park.auth.domain.authenticator.AuthenticatorManager
import io.github.siyual_park.auth.domain.authenticator.AuthorizationAuthenticator
import io.github.siyual_park.auth.domain.authenticator.AuthorizationProcessor
import io.github.siyual_park.auth.domain.authenticator.MatchTypeAuthenticateFilter
import org.springframework.beans.BeansException
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
            it.javaClass.annotations.filter { it is AuthenticateMapping }
                .forEach { annotation ->
                    if (annotation !is AuthenticateMapping) return@forEach
                    val filterBeen = try {
                        applicationContext.getBean(annotation.filterBy.java)
                    } catch (e: BeansException) {
                        null
                    }
                    val filter = when (filterBeen) {
                        null -> {
                            MatchTypeAuthenticateFilter(annotation.filterBy.java)
                        }
                        is AuthenticateFilter -> {
                            filterBeen
                        }
                        else -> {
                            return@forEach
                        }
                    }
                    authenticatorManager.register(filter, it)
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
