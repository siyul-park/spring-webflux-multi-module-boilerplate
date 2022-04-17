package io.github.siyual_park.data.converter

import io.github.siyual_park.data.annotation.ConverterScope
import org.bson.types.Binary
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

@Component
@WritingConverter
@ConverterScope(ConverterScope.Type.MONGO)
class BytesToBinaryConverter : Converter<ByteArray, Binary> {
    override fun convert(source: ByteArray): Binary {
        return Binary(source)
    }
}
