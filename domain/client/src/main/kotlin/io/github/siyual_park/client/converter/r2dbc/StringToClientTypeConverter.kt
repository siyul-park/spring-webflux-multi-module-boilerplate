package io.github.siyual_park.client.converter.r2dbc

import io.github.siyual_park.client.entity.ClientType
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.stereotype.Component

@Component
@ReadingConverter
class StringToClientTypeConverter : Converter<String, ClientType> {
    override fun convert(source: String): ClientType? {
        return ClientType.values().find { it.value == source }
    }
}
