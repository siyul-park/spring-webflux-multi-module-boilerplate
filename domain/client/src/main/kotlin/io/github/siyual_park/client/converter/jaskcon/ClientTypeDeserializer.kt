package io.github.siyual_park.client.converter.jaskcon

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import io.github.siyual_park.client.entity.ClientType

class ClientTypeDeserializer : JsonDeserializer<ClientType>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ClientType? {
        return ClientType.values().find { it.value == p.text.lowercase() }
    }
}
