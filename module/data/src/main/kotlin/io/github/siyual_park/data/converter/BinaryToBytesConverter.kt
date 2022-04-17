package io.github.siyual_park.data.converter

import org.bson.types.Binary
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

@Component
@WritingConverter
@ReadingConverter
class BinaryToBytesConverter : Converter<Binary, ByteArray> {
    override fun convert(source: Binary): ByteArray {
        return source.data
    }
}
