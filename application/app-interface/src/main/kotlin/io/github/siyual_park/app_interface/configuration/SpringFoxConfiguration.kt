package io.github.siyual_park.app_interface.configuration

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.time.Instant

@Configuration
@EnableSwagger2
class SpringFoxConfiguration {
    @Bean
    fun api(): Docket {
        return Docket(DocumentationType.SWAGGER_2).apply {
            directModelSubstitute(Instant::class.java, Long::class.java)
        }
            .select()
            .apis(
                RequestHandlerSelectors.withClassAnnotation(Api::class.java)
                    .or(RequestHandlerSelectors.withMethodAnnotation(ApiOperation::class.java))
            )
            .paths(PathSelectors.any())
            .build()
    }
}
