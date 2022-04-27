package io.github.siyual_park.application.server.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.siyual_park.search.pagination.OffsetPage
import io.github.siyual_park.ulid.ULID
import io.swagger.v3.core.jackson.ModelResolver
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.SpringDocUtils.getConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.time.Instant

@Configuration
class SwaggerConfiguration {
    init {
        getConfig()
            .replaceWithClass(Instant::class.java, Long::class.java)
            .replaceWithClass(Duration::class.java, Long::class.java)
            .replaceWithClass(ULID::class.java, String::class.java)
            .replaceWithClass(OffsetPage::class.java, List::class.java)
    }

    @Bean
    fun modelResolver(objectMapper: ObjectMapper): ModelResolver {
        return ModelResolver(objectMapper)
    }

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .components(
                Components()
                    .addSecuritySchemes(
                        "bearer",
                        SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer")
                    )
            )
    }
}
