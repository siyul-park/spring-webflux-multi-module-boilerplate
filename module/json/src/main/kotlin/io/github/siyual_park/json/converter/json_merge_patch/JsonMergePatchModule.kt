package io.github.siyual_park.json.converter.json_merge_patch

import com.fasterxml.jackson.databind.module.SimpleModule
import io.github.siyual_park.json.patch.JsonMergePatch
import org.springframework.stereotype.Component

@Component
class JsonMergePatchModule(
    jsonPatchDeserializer: JsonMergePatchDeserializer
) : SimpleModule() {
    init {
        addDeserializer(JsonMergePatch::class.java, jsonPatchDeserializer)
    }
}
