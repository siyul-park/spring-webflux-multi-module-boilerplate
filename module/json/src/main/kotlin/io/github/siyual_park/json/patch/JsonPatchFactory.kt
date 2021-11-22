package io.github.siyual_park.json.patch

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class JsonPatchFactory(
    private val objectMapper: ObjectMapper
) {
    fun <T : Any> create(content: String): JsonPatch<T> {
        return JsonPatch(objectMapper.readTree(content), objectMapper)
    }

    fun <T : Any> create(parser: JsonParser): JsonPatch<T> {
        return JsonPatch(objectMapper.readTree(parser), objectMapper)
    }

    fun <T : Any> create(node: JsonNode): JsonPatch<T> {
        return JsonPatch(node, objectMapper)
    }
}
