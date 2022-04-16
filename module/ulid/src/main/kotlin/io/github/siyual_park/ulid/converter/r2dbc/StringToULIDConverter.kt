package io.github.siyual_park.ulid.converter.r2dbc

import io.github.siyual_park.ulid.ULID
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.stereotype.Component

@Component
@ReadingConverter
class StringToULIDConverter : Converter<String, ULID> {
    override fun convert(source: String): ULID {
        return ULID.fromString(source)
    }
}
