package io.github.siyual_park.auth.configuration

import io.github.siyual_park.auth.spring.AuthenticationConverter
import io.github.siyual_park.auth.spring.AuthenticationManager
import io.github.siyual_park.auth.spring.ScopeEvaluator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
class SecurityConfiguration(
    private val scopeEvaluator: ScopeEvaluator,
    private val authenticationManager: AuthenticationManager,
    private val authenticationConverter: AuthenticationConverter
) {

    @Bean
    fun defaultMethodSecurityExpressionHandler(): DefaultMethodSecurityExpressionHandler {
        val expressionHandler = DefaultMethodSecurityExpressionHandler()
        expressionHandler.setPermissionEvaluator(scopeEvaluator)
        return expressionHandler
    }

    @Bean
    fun securityWebFilterChain(httpSecurity: ServerHttpSecurity): SecurityWebFilterChain {
        val authenticationWebFilter = AuthenticationWebFilter(authenticationManager)
        authenticationWebFilter.setServerAuthenticationConverter(authenticationConverter)

        return httpSecurity
            .csrf().disable()
            .formLogin().disable()
            .httpBasic().disable()
            .authorizeExchange {
                it.anyExchange().permitAll()
            }
            .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
    }
}
