package io.github.siyual_park.json.patch

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class JsonPatchFactory(
    private val objectMapper: ObjectMapper,
) {
    fun <T, U> create(overrides: U) = JsonPatch<T, U>(objectMapper, overrides)
}
