package io.github.siyual_park.data.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component
import java.net.URL

@Component
@WritingConverter
class URLToStringConverter : Converter<URL, String> {
    override fun convert(source: URL): String {
        return source.toString()
    }
}
