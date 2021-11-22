package io.github.siyual_park.json.converter.json_patch

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import io.github.siyual_park.json.patch.JsonPatch
import io.github.siyual_park.json.patch.JsonPatchFactory
import org.springframework.stereotype.Component

@Component
class JsonPatchDeserializer(
    private val jsonPatchFactory: JsonPatchFactory
) : JsonDeserializer<JsonPatch<*>>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): JsonPatch<*> {
        return jsonPatchFactory.create<Any>(p)
    }
}
