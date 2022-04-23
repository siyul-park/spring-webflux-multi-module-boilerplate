package io.github.siyual_park.auth.configuration

import io.github.siyual_park.auth.domain.AuthenticationConverter
import io.github.siyual_park.auth.domain.AuthenticationManager
import io.github.siyual_park.auth.domain.ScopeEvaluator
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.web.cors.reactive.CorsConfigurationSource

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfiguration(
    private val applicationContext: ApplicationContext,
    private val authenticationManager: AuthenticationManager,
    private val authenticationConverter: AuthenticationConverter,
    private val scopeEvaluator: ScopeEvaluator,
    private val corsConfigurationSource: CorsConfigurationSource
) {
    @Bean
    @DependsOn("methodSecurityExpressionHandler")
    fun securityWebFilterChain(httpSecurity: ServerHttpSecurity): SecurityWebFilterChain {
        val defaultWebSecurityExpressionHandler = this.applicationContext.getBean(
            DefaultMethodSecurityExpressionHandler::class.java
        )
        defaultWebSecurityExpressionHandler.setPermissionEvaluator(scopeEvaluator)

        val authenticationWebFilter = AuthenticationWebFilter(authenticationManager)
        authenticationWebFilter.setServerAuthenticationConverter(authenticationConverter)

        return httpSecurity
            .cors().configurationSource(corsConfigurationSource).and()
            .csrf().disable()
            .formLogin().disable()
            .httpBasic().disable()
            .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .authorizeExchange { it.anyExchange().permitAll() }
            .build()
    }
}
