package io.github.siyual_park.ulid.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import io.github.siyual_park.ulid.ULID

class ULIDDeserializer : JsonDeserializer<ULID>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ULID {
        return ULID.fromString(p.valueAsString)
    }
}
