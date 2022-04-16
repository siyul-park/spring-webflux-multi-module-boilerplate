package io.github.siyual_park.ulid.converter

import io.github.siyual_park.ulid.ULID
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

@Component
@WritingConverter
class ULIDToBytesConverter : Converter<ULID, ByteArray> {
    override fun convert(source: ULID): ByteArray {
        return source.toBytes()
    }
}
