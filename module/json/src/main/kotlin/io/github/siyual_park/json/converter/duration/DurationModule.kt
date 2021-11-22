package io.github.siyual_park.json.converter.duration

import com.fasterxml.jackson.databind.module.SimpleModule
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class DurationModule : SimpleModule() {
    init {
        addSerializer(Duration::class.java, DurationSerializer())
        addDeserializer(Duration::class.java, DurationDeserializer())
    }
}
