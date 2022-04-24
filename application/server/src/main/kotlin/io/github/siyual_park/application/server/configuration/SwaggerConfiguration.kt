package io.github.siyual_park.application.server.configuration

import io.github.siyual_park.search.pagination.OffsetPage
import io.github.siyual_park.ulid.ULID
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.tags.Tag
import org.springdoc.core.SpringDocUtils.getConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfiguration {
    init {
        getConfig()
            .replaceWithClass(ULID::class.java, String::class.java)
            .replaceWithClass(OffsetPage::class.java, List::class.java)
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
            ).addTagsItem(Tag().name("utility"))
    }
}
