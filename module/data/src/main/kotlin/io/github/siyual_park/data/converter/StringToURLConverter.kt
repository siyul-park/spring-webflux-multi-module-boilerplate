package io.github.siyual_park.data.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.stereotype.Component
import java.net.URL

@Component
@ReadingConverter
class StringToURLConverter : Converter<String, URL> {
    override fun convert(source: String): URL {
        return URL(source)
    }
}
