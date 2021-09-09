package io.github.siyual_park.app_interface.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import io.github.siyual_park.app_interface.instant.InstantEpochTimeModule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfiguration {
    @Autowired(required = true)
    fun configObjectMapper(objectMapper: ObjectMapper) {
        objectMapper.apply {
            registerModule(Jdk8Module())
            registerModule(InstantEpochTimeModule())

            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        }
    }
}
