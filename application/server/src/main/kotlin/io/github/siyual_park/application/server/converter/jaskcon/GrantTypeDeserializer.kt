package io.github.siyual_park.application.server.converter.jaskcon

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import io.github.siyual_park.application.server.dto.GrantType

class GrantTypeDeserializer : JsonDeserializer<GrantType>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): GrantType? {
        return GrantType.values().find { it.value == p.text.lowercase() }
    }
}
