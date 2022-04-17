package io.github.siyual_park.data.converter

import io.github.siyual_park.data.annotation.ConverterScope
import io.github.siyual_park.ulid.ULID
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.stereotype.Component

@Component
@ReadingConverter
@ConverterScope(ConverterScope.Type.R2DBC)
class BytesToULIDConverter : Converter<ByteArray, ULID> {
    override fun convert(source: ByteArray): ULID {
        return ULID.fromBytes(source)
    }
}
