package io.github.siyual_park.swagger.configuration

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
    fun api(): springfox.documentation.spring.web.plugins.Docket {
        return springfox.documentation.spring.web.plugins.Docket(springfox.documentation.spi.DocumentationType.SWAGGER_2)
            .apply {
                directModelSubstitute(Instant::class.java, Long::class.java)
            }
            .select()
            .apis(
                springfox.documentation.builders.RequestHandlerSelectors.withClassAnnotation(io.swagger.annotations.Api::class.java)
                    .or(springfox.documentation.builders.RequestHandlerSelectors.withMethodAnnotation(io.swagger.annotations.ApiOperation::class.java))
            )
            .paths(springfox.documentation.builders.PathSelectors.any())
            .build()
    }
}
