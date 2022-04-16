package io.github.siyual_park.ulid.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import io.github.siyual_park.ulid.ULID

class ULIDSerializer : JsonSerializer<ULID>() {
    override fun serialize(value: ULID, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.toString())
    }
}
