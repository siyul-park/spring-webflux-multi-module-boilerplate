package io.github.siyual_park.auth.configuration

import io.github.siyual_park.auth.domain.authentication.AuthenticateFilter
import io.github.siyual_park.auth.domain.authentication.AuthenticateMapping
import io.github.siyual_park.auth.domain.authentication.AuthenticatePipeline
import io.github.siyual_park.auth.domain.authentication.AuthenticateStrategy
import io.github.siyual_park.auth.domain.authentication.Authenticator
import io.github.siyual_park.auth.domain.authentication.TypeMatchAuthenticateFilter
import org.springframework.beans.BeansException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.AnnotationAwareOrderComparator

@Configuration
class AuthenticationConfiguration(
    private val applicationContext: ApplicationContext
) {
    @Autowired(required = true)
    fun configAuthenticator(authenticator: Authenticator) {
        applicationContext.getBeansOfType(AuthenticateStrategy::class.java).values
            .sortedWith(AnnotationAwareOrderComparator.INSTANCE)
            .forEach {
                it.javaClass.annotations.filterIsInstance<AuthenticateMapping>()
                    .forEach { annotation ->
                        val filter = getFilter(annotation)
                        authenticator.register(filter, it)
                    }
            }

        applicationContext.getBeansOfType(AuthenticatePipeline::class.java).values
            .sortedWith(AnnotationAwareOrderComparator.INSTANCE)
            .forEach {
                it.javaClass.annotations.filterIsInstance<AuthenticateMapping>()
                    .forEach { annotation ->
                        val filter = getFilter(annotation)
                        authenticator.register(filter, it)
                    }
            }
    }

    private fun getFilter(mapping: AuthenticateMapping): AuthenticateFilter {
        val filterBeen = try {
            applicationContext.getBean(mapping.filterBy.java)
        } catch (e: BeansException) {
            null
        }
        return if (filterBeen is AuthenticateFilter) {
            filterBeen
        } else {
            TypeMatchAuthenticateFilter(mapping.filterBy)
        }
    }
}
