package io.github.siyual_park.application.external.converter.spring

import io.github.siyual_park.application.external.dto.GrantType
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class GrantTypeConverter : Converter<String, GrantType> {
    override fun convert(source: String): GrantType? {
        return GrantType.values().find { it.value == source.lowercase() }
    }
}
