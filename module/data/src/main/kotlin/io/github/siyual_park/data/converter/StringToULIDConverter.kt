package io.github.siyual_park.data.converter

import io.github.siyual_park.ulid.ULID
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class StringToULIDConverter : Converter<String, ULID> {
    override fun convert(source: String): ULID {
        return ULID.fromString(source)
    }
}
