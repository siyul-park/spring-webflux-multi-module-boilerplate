package io.github.siyual_park.json.patch

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.siyual_park.data.patch.Patch

class JsonPatch<T, U>(
    private val objectMapper: ObjectMapper,
    private val overrides: U
) : Patch<T> {
    override fun apply(entity: T): T {
        return objectMapper.updateValue(entity, overrides)
    }
}
