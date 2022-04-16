package io.github.siyual_park.ulid.converter.r2dbc

import io.github.siyual_park.ulid.ULID
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.stereotype.Component

@Component
@ReadingConverter
class BytesToULIDConverter : Converter<ByteArray, ULID> {
    override fun convert(source: ByteArray): ULID {
        return ULID.fromBytes(source)
    }
}
