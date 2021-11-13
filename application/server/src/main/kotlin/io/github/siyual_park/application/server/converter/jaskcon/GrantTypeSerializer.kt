package io.github.siyual_park.application.server.converter.jaskcon

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import io.github.siyual_park.application.server.dto.GrantType

class GrantTypeSerializer : JsonSerializer<GrantType>() {
    override fun serialize(value: GrantType, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.value)
    }
}
