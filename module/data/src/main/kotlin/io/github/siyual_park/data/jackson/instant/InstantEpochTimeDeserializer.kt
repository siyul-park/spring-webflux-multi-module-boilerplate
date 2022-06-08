package io.github.siyual_park.data.jackson.instant

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.time.Instant

class InstantEpochTimeDeserializer : JsonDeserializer<Instant>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Instant {
        return Instant.ofEpochSecond(p.longValue)
    }
}
