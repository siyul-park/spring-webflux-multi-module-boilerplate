package io.github.siyual_park.json.converter.json_patch

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.siyual_park.json.patch.JsonPatch
import org.springframework.stereotype.Component

@Component
class JsonPatchDeserializer(
    private val objectMapper: ObjectMapper
) : JsonDeserializer<JsonPatch<*>>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): JsonPatch<*> {
        return JsonPatch<Any>(
            objectMapper.readTree(p),
            objectMapper
        )
    }
}
