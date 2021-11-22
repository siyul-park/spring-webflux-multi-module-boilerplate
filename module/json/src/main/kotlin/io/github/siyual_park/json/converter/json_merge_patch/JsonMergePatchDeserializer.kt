package io.github.siyual_park.json.converter.json_merge_patch

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import io.github.siyual_park.json.patch.JsonMergePatch
import io.github.siyual_park.json.patch.JsonMergePatchFactory
import org.springframework.stereotype.Component

@Component
class JsonMergePatchDeserializer(
    private val jsonMergePatchFactory: JsonMergePatchFactory
) : JsonDeserializer<JsonMergePatch<*>>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): JsonMergePatch<*> {
        return jsonMergePatchFactory.create<Any>(p)
    }
}
