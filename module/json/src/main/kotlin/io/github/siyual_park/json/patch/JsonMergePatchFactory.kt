package io.github.siyual_park.json.patch

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class JsonMergePatchFactory(
    private val objectMapper: ObjectMapper
) {
    fun <T : Any> create(content: String): JsonMergePatch<T> {
        return JsonMergePatch(objectMapper.readTree(content), objectMapper)
    }

    fun <T : Any> create(parser: JsonParser): JsonMergePatch<T> {
        return JsonMergePatch(objectMapper.readTree(parser), objectMapper)
    }

    fun <T : Any> create(node: JsonNode): JsonMergePatch<T> {
        return JsonMergePatch(node, objectMapper)
    }
}
