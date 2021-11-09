package io.github.siyual_park.data.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@WritingConverter
class DurationToLongConverter : Converter<Duration, Long> {
    override fun convert(source: Duration): Long {
        return source.seconds
    }
}
