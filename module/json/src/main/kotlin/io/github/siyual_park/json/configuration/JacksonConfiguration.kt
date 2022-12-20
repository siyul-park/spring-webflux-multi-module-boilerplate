package io.github.siyual_park.json.configuration

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfiguration(
    private val applicationContext: ApplicationContext
) {
    @Bean
    fun objectMapper(): ObjectMapper {
        return jacksonObjectMapper().apply {
            val modules = applicationContext.getBeansOfType(Module::class.java)
            registerModule(Jdk8Module())
            modules.values.forEach {
                registerModule(it)
            }
            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        }
    }
}
