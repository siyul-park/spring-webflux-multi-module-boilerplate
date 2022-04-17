package io.github.siyual_park.data.converter

import io.github.siyual_park.data.annotation.ConverterScope
import io.github.siyual_park.ulid.ULID
import org.bson.types.Binary
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.stereotype.Component

@Component
@ReadingConverter
@ConverterScope(ConverterScope.Type.MONGO)
class BinaryToULIDConverter : Converter<Binary, ULID> {
    override fun convert(source: Binary): ULID {
        return ULID.fromBytes(source.data)
    }
}
