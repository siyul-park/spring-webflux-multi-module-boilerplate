package io.github.siyual_park.data.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@ReadingConverter
class LongToDurationConverter : Converter<Long, Duration> {
    override fun convert(source: Long): Duration {
        return Duration.ofSeconds(source)
    }
}
