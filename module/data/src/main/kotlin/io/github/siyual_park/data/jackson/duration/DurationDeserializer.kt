package io.github.siyual_park.data.jackson.duration

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.time.Duration

class DurationDeserializer : JsonDeserializer<Duration>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Duration {
        return Duration.ofSeconds(p.longValue)
    }
}
