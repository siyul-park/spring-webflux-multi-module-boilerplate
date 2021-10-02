package io.github.siyual_park.data.converter

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

@Component
@WritingConverter
class MapToStringConverter(
    private val objectMapper: ObjectMapper
) : Converter<Map<String, Any>, String> {
    override fun convert(source: Map<String, Any>): String {
        return objectMapper.writeValueAsString(source)
    }
}
