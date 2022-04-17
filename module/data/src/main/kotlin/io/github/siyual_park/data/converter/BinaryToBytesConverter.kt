package io.github.siyual_park.data.converter

import io.github.siyual_park.data.annotation.ConverterScope
import org.bson.types.Binary
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.stereotype.Component

@Component
@ReadingConverter
@ConverterScope(ConverterScope.Type.MONGO)
class BinaryToBytesConverter : Converter<Binary, ByteArray> {
    override fun convert(source: Binary): ByteArray {
        return source.data
    }
}
