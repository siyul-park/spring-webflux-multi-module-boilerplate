package io.github.siyual_park.ulid.converter

import io.github.siyual_park.ulid.ULID
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

@Component
@WritingConverter
class ULIDToStringConverter : Converter<ULID, String> {
    override fun convert(source: ULID): String {
        return source.toString()
    }
}
