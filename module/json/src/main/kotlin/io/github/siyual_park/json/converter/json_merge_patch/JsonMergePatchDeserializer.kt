package io.github.siyual_park.json.converter.json_merge_patch

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.siyual_park.json.patch.JsonMergePatch
import org.springframework.stereotype.Component

@Component
class JsonMergePatchDeserializer(
    private val objectMapper: ObjectMapper
) : JsonDeserializer<JsonMergePatch<*>>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): JsonMergePatch<*> {
        return JsonMergePatch<Any>(
            objectMapper.readTree(p),
            objectMapper
        )
    }
}
