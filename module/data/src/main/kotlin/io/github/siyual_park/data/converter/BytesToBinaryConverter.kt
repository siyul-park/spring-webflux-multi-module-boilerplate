package io.github.siyual_park.data.converter

import org.bson.types.Binary
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

@Component
@WritingConverter
@ReadingConverter
class BytesToBinaryConverter : Converter<ByteArray, Binary> {
    override fun convert(source: ByteArray): Binary {
        return Binary(source)
    }
}
