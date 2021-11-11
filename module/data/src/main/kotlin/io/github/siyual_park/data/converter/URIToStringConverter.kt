package io.github.siyual_park.data.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component
import java.net.URI

@Component
@WritingConverter
class URIToStringConverter : Converter<URI, String> {
    override fun convert(source: URI): String {
        return source.toString()
    }
}
