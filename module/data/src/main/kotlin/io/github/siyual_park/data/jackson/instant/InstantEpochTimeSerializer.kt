package io.github.siyual_park.data.jackson.instant

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.time.Instant

class InstantEpochTimeSerializer : JsonSerializer<Instant>() {
    override fun serialize(value: Instant, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeNumber(value.epochSecond)
    }
}
