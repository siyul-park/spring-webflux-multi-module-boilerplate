package io.github.siyual_park.data.configuration

import io.github.siyual_park.data.annotation.ConverterScope
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions

@Configuration
class MongoConfiguration(
    private val applicationContext: ApplicationContext
) {
    @Bean
    fun mongoCustomConversions(): MongoCustomConversions {
        val converters = applicationContext.getBeansOfType(Converter::class.java)
            .values
            .filter { it.javaClass.annotations.any { it is WritingConverter || it is ReadingConverter } }
            .filter {
                val scope = it.javaClass.annotations.filterIsInstance<ConverterScope>()
                scope.isEmpty() || scope.any { it.type == ConverterScope.Type.MONGO }
            }

        return MongoCustomConversions(converters)
    }
}
