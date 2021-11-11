package io.github.siyual_park.data.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.stereotype.Component
import java.net.URI

@Component
@ReadingConverter
class StringToURIConverter : Converter<String, URI> {
    override fun convert(source: String): URI {
        return URI(source)
    }
}
