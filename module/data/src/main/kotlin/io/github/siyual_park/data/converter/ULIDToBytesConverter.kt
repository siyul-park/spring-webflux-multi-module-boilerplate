package io.github.siyual_park.data.converter

import io.github.siyual_park.data.annotation.ConverterScope
import io.github.siyual_park.ulid.ULID
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

@Component
@WritingConverter
@ConverterScope(ConverterScope.Type.R2DBC)
class ULIDToBytesConverter : Converter<ULID, ByteArray> {
    override fun convert(source: ULID): ByteArray {
        return source.toBytes()
    }
}
