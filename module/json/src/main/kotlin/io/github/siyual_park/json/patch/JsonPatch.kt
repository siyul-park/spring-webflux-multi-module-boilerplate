package io.github.siyual_park.json.patch

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonpatch.JsonPatch
import io.github.siyual_park.data.patch.Patch
import javax.validation.ValidationException

class JsonPatch<T : Any>(
    node: JsonNode,
    private val objectMapper: ObjectMapper
) : Patch<T> {
    private val patch by lazy {
        JsonPatch.fromJson(node)
    }

    override fun apply(entity: T): T {
        try {
            val node: JsonNode = objectMapper.valueToTree(entity)
            val result = patch.apply(node)
            return objectMapper.treeToValue(result, entity.javaClass)
        } catch (e: RuntimeException) {
            throw ValidationException(e)
        }
    }
}
