package io.github.siyual_park.client.converter.r2dbc

import io.github.siyual_park.client.entity.ClientType
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

@Component
@WritingConverter
class ClientTypeToStringConverter : Converter<ClientType, String> {
    override fun convert(source: ClientType): String {
        return source.value
    }
}
