package io.github.siyual_park.swagger.configuration

import com.fasterxml.classmate.TypeResolver
import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.siyual_park.ulid.ULID
import io.swagger.annotations.Api
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.annotation.AuthenticationPrincipal
import springfox.documentation.annotations.ApiIgnore
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.schema.AlternateTypeRules.newRule
import springfox.documentation.service.ApiKey
import springfox.documentation.service.AuthorizationScope
import springfox.documentation.service.SecurityReference
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.time.Instant

@Configuration
@EnableSwagger2
class SpringFoxConfiguration {
    @Bean
    fun api(): Docket {
        val typeResolver = TypeResolver()

        return Docket(DocumentationType.SWAGGER_2)
            .apply {
                directModelSubstitute(Instant::class.java, Long::class.java)
            }
            .select()
            .apis(
                RequestHandlerSelectors.withClassAnnotation(Api::class.java)
                    .or(RequestHandlerSelectors.withMethodAnnotation(io.swagger.annotations.ApiOperation::class.java))
            )
            .paths(PathSelectors.any())
            .build()
            .ignoredParameterTypes(ApiIgnore::class.java, AuthenticationPrincipal::class.java, JsonIgnore::class.java)
            .alternateTypeRules(
                newRule(
                    typeResolver.resolve(ULID::class.java),
                    typeResolver.resolve(String::class.java)
                )
            )
            .securitySchemes(listOf(apiKey()))
            .securityContexts(listOf(securityContext()))
    }

    private fun securityContext(): SecurityContext {
        return SecurityContext.builder().securityReferences(defaultAuth()).build()
    }

    private fun apiKey(): ApiKey {
        return ApiKey("Authorization", "Authorization", "header")
    }

    private fun defaultAuth(): List<SecurityReference> {
        val authorizationScope = AuthorizationScope("global", "access All")
        return listOf(SecurityReference("Authorization", arrayOf(authorizationScope)))
    }
}
