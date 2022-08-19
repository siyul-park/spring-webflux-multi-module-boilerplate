package io.github.siyual_park.auth.configuration

import io.github.siyual_park.auth.domain.AuthenticationConverter
import io.github.siyual_park.auth.domain.AuthenticationManager
import io.github.siyual_park.auth.domain.cors.CorsSpec
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfiguration(
    private val applicationContext: ApplicationContext,
    private val authenticationManager: AuthenticationManager,
    private val authenticationConverter: AuthenticationConverter,
) {
    @Bean
    @DependsOn("methodSecurityExpressionHandler")
    fun securityWebFilterChain(httpSecurity: ServerHttpSecurity): SecurityWebFilterChain {
        val authenticationWebFilter = AuthenticationWebFilter(authenticationManager)
        authenticationWebFilter.setServerAuthenticationConverter(authenticationConverter)
        authenticationWebFilter.setAuthenticationFailureHandler { _, exception -> Mono.error(exception) }

        val corsSpec = CorsSpec(applicationContext)

        return httpSecurity
            .cors().disable()
            .csrf().disable()
            .formLogin().disable()
            .logout().disable()
            .httpBasic().disable()
            .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .authorizeExchange { it.anyExchange().permitAll() }
            .also { corsSpec.configure(it) }
            .build()
    }
}
